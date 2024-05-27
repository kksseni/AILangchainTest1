package com.nure.ua.aiconsultant.config;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ConversationalRetrievalConfiguration {
    private final ChatLanguageModel chatLanguageModel;
    private final Document document;
    private final EmbeddingModel embeddingModel;
    private final PromptTemplate promptTemplate;

    @Value("${openai.model}")
    private String modelName;

    public ConversationalRetrievalConfiguration(ChatLanguageModel chatLanguageModel, Document document, EmbeddingModel embeddingModel, PromptTemplate promptTemplate) {
        this.chatLanguageModel = chatLanguageModel;
        this.document = document;
        this.embeddingModel = embeddingModel;
        this.promptTemplate = promptTemplate;
    }

    @Bean
    public DocumentSplitter documentSplitter() {
        return DocumentSplitters.recursive(1024, 0, new OpenAiTokenizer(modelName));
    }

    @Bean
    public List<TextSegment> textSegments(DocumentSplitter splitter) {
        return splitter.split(document);
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(List<TextSegment> segments) {
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        store.addAll(embeddings, segments);
        return store;
    }

    @Bean
    public ConversationalRetrievalChain conversationalRetrievalChain(EmbeddingStore<TextSegment> embeddingStore) {
        return ConversationalRetrievalChain.builder()
                .chatLanguageModel(chatLanguageModel)
                .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
                .chatMemory(MessageWindowChatMemory.withMaxMessages(3))
                .promptTemplate(promptTemplate)
                .build();
    }
}


