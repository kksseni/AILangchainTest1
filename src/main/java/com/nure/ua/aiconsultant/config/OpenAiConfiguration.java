package com.nure.ua.aiconsultant.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.FileSystemDocumentLoader;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenAiConfiguration {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.model}")
    private String modelName;

    public static final String docPath = "src/main/resources/Dataset_Informatics_Department .docx";

    public static final String promptTemplate = """
                    Ти консультант для абітурієнтів у чаті на сайті кафедри Інформатики Харківського Університету Радіоелектроніки, яка навчає студентів за спеціальністю Комп'ютерні науки, освітньо-професійна програма Інформатика. У тебе є інформація про спеціальність Комп'ютерні науки, освітньо-професійна програма Інформатика, також про саму кафедру Інформатика, яка є провідною для програми Інформатика, її наукову діяльність та інші питання. Тобі ставить запитання абітурієнт у чаті, дай йому відповідь, спираючись на документ, відповідай максимально точно за документом, не вигадуй нічого від себе. Не згадуй документ під час відповіді, абітурієнт нічого не повинен знати про документ, за яким ти відповідаєш. Відповідай так, щоб абітурієнт захотів вступити на спеціальність Комп'ютерні науки, освітньо-професійна програма Інформатика. Дай відповідь не більше, ніж на 3000 знаків.\s
                    Дай відповідь на питання: {{question}}
                    Документ з інформацією для відповіді клієнту: \n{{information}}
                    Надай відповідь мовою, якою поставлено запитання.
                    Якщо в документі з інформацією немає нічого по запитанню, пиши що не володієшь цією іформацією.
            """;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName(modelName)
                .temperature(0.001)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public Document document() {
        return FileSystemDocumentLoader.loadDocument(docPath);
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2QuantizedEmbeddingModel();
    }

    @Bean
    public PromptTemplate promptTemplate() {
        return PromptTemplate.from(promptTemplate);
    }
}


