package com.nure.ua.aiconsultant.service;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.FileSystemDocumentLoader;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO_0301;

@Service
public class LLMService {
    public static final String DOC_PATH = "src/main/resources/Dataset_Informatics_Department .docx";
    public static final String HTML_PATH = "src/main/resources/info.html";

    public static final String PROMPT = """
                    Ти консультант для абітурієнтів у чаті на сайті кафедри Інформатики Харківського Університету Радіоелектроніки, яка навчає студентів за спеціальністю Комп'ютерні науки, освітньо-професійна програма Інформатика. У тебе є інформація про спеціальність Комп'ютерні науки, освітньо-професійна програма Інформатика, також про саму кафедру Інформатика, яка є провідною для програми Інформатика, її наукову діяльність та інші питання. Тобі ставить запитання абітурієнт у чаті, дай йому відповідь, спираючись на документ, відповідай максимально точно за документом, не вигадуй нічого від себе. Не згадуй документ під час відповіді, абітурієнт нічого не повинен знати про документ, за яким ти відповідаєш. Відповідай так, щоб абітурієнт захотів вступити на спеціальність Комп'ютерні науки, освітньо-професійна програма Інформатика. Дай відповідь не більше, ніж на 3000 знаків.\s
                    Дай выдповыдь на питання: {{question}}
                    Документ з інформацією для відповіді клієнту: \n{{information}}
            """;

    @Value("${openai.api.key}")
    String openaiApiKey;

    public String getRelatedContent(String question) {

        PromptTemplate promptTemplate = PromptTemplate
                .from("Tell me a {{adjective}} joke about {{content}}..");
        Map<String, Object> variables = new HashMap<>();
        variables.put("adjective", "funny");
        variables.put("content", "computers");
        Prompt prompt = promptTemplate.apply(variables);


        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName(GPT_3_5_TURBO)
                .temperature(0.3)
                .build();
        String response = model.generate(prompt.text());

        ChatMemory chatMemory = TokenWindowChatMemory
                .withMaxTokens(300, new OpenAiTokenizer(GPT_3_5_TURBO));
        chatMemory.add(userMessage("Hello, my name is Kumar"));
        AiMessage answer = model.generate(chatMemory.messages()).content();
        System.out.println(answer.text()); // Hello Kumar! How can I assist you today?
        chatMemory.add(answer);
        chatMemory.add(userMessage("What is my name?"));
        AiMessage answerWithName = model.generate(chatMemory.messages()).content();
        System.out.println(answer.text()); // Your name is Kumar.
        chatMemory.add(answerWithName);

        return "";
    }

    public List<EmbeddingMatch<TextSegment>> getContentFromDoc2(String question) {

        Document document = FileSystemDocumentLoader.loadDocument(DOC_PATH);
        DocumentSplitter splitter = DocumentSplitters.recursive(100, 0,
                new OpenAiTokenizer(GPT_3_5_TURBO));
        List<TextSegment> segments = splitter.split(document);

        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);

        //The assumption here is that the answer to this question is contained in the document we processed earlier.
        Embedding questionEmbedding = embeddingModel.embed(question).content();
        int maxResults = 6;
        double minScore = 0.7;
        List<EmbeddingMatch<TextSegment>> relevantEmbeddings = embeddingStore
                .findRelevant(questionEmbedding, maxResults, minScore);
        return relevantEmbeddings;
    }

    public List<String> getContentFromDoc(String question) {
        Document document1 = FileSystemDocumentLoader.loadDocument(DOC_PATH);
        String fullText = document1.text();
        List<TextSegment> segments = convertStringToTextSegment(convertHtmlToText(splitText(fullText)));

        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);

        //The assumption here is that the answer to this question is contained in the document we processed earlier.
        Embedding questionEmbedding = embeddingModel.embed(question).content();
        int maxResults = 3;
        double minScore = 0.7;
        List<EmbeddingMatch<TextSegment>> relevantEmbeddings = embeddingStore
                .findRelevant(questionEmbedding, maxResults, minScore);

        List<String> relevantTextSegments1 = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : relevantEmbeddings) {
            relevantTextSegments1.add(match.embedded().text());
        }
        return relevantTextSegments1;
    }

    private static final Pattern SUBHEADING_PATTERN = Pattern.compile("(?<=<h1>)([\\s\\S]*?)(?=<h1>)", Pattern.DOTALL);

    public List<String> splitText(String text) {
        List<String> parts = new ArrayList<>();
        String[] splitByH1 = text.split("(?=<h1>)");

        for (String part : splitByH1) {
            if (!part.isEmpty()) {
                parts.add(part);
            }
        }

        return parts;
    }

    public List<String> convertHtmlToText(List<String> segments) {
        segments.replaceAll(html -> Jsoup.parse(html).text());
        return segments;
    }
    public List<TextSegment> convertStringToTextSegment(List<String> segments) {
        List<TextSegment> textSegments = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++){
            Map<String, String> data = new HashMap<>();
            data.put("absolute_directory_path","C:\\Users\\User\\IdeaProjects\\AIConsultant\\src\\main\\resources");
            data.put("index", String.valueOf(i));
            data.put("file_name","info.html");
            data.put("document_type","HTML");
            Metadata metadata = new Metadata(data);
            textSegments.add(new TextSegment(segments.get(i), metadata));
        }
        segments.replaceAll(html -> Jsoup.parse(html).text());
        return textSegments;
    }

    public List<String> getContentFromDoc1(String question) {
        Document document = FileSystemDocumentLoader.loadDocument(DOC_PATH);
        String fullText = document.text();

        // Разбиваем текст по тегам <h1>
        List<String> segments = convertHtmlToText(splitText(fullText));

        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        List<Embedding> embeddings = new ArrayList<>();
        for (String segment : segments) {
            embeddings.add(embeddingModel.embed(segment).content());
        }

        EmbeddingStore<String> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);

        Embedding questionEmbedding = embeddingModel.embed(question).content();
        int maxResults = 3;
        double minScore = 0.7;
        List<EmbeddingMatch<String>> relevantEmbeddings = embeddingStore
                .findRelevant(questionEmbedding, maxResults, minScore);

        List<String> relevantTextSegments = new ArrayList<>();
        for (EmbeddingMatch<String> match : relevantEmbeddings) {
            relevantTextSegments.add(match.embedded());
        }

        return relevantTextSegments;
    }

    public String getContentConvModel(String question) {

        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName(GPT_3_5_TURBO)
                .temperature(0.3)
                .build();

        Document document = FileSystemDocumentLoader.loadDocument(DOC_PATH);
        DocumentSplitter splitter = DocumentSplitters.recursive(1024, 0,
                new OpenAiTokenizer(GPT_3_5_TURBO_0301));
        List<TextSegment> segments = splitter.split(document);

        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);

        ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
                .chatLanguageModel(model)
                .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
                .chatMemory(MessageWindowChatMemory.withMaxMessages(3))
                .promptTemplate(PromptTemplate
                        .from(PROMPT))
                .build();
        String answer = chain.execute(question);
        return answer;
    }

}
