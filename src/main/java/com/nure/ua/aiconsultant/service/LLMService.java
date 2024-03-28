package com.nure.ua.aiconsultant.service;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;

@Service
public class LLMService {
    public static final String DOC_PATH = "src/main/resources/Dataset_Informatics_Department .docx";

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

    public List<EmbeddingMatch<TextSegment>> getContentFromDoc(String question) {

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

    public String getContentConvModel(String question) {

        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName(GPT_3_5_TURBO)
                .temperature(0.3)
                .build();

        Document document = FileSystemDocumentLoader.loadDocument(DOC_PATH);
        DocumentSplitter splitter = DocumentSplitters.recursive(10, 0,
                new OpenAiTokenizer(GPT_3_5_TURBO));
        List<TextSegment> segments = splitter.split(document);

        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);

        ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
                .chatLanguageModel(model)
                .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
                //.chatMemory(MessageWindowChatMemory.withMaxMessages(3))
                .promptTemplate(PromptTemplate
                        .from(PROMPT))
                .build();
        String answer = chain.execute(question);
        return answer;
    }

}
