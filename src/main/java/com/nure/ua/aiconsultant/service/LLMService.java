package com.nure.ua.aiconsultant.service;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import org.springframework.stereotype.Service;

@Service
public class LLMService {

    private final ConversationalRetrievalChain conversationalRetrievalChain;

    public LLMService(ConversationalRetrievalChain conversationalRetrievalChain) {
        this.conversationalRetrievalChain = conversationalRetrievalChain;
    }

    public String getContentConvModel(String question) {
        return conversationalRetrievalChain.execute(question);
    }

}
