package com.nure.ua.aiconsultant.controller;

import com.nure.ua.aiconsultant.service.LLMService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/bot")
public class CustomBotController {


    private final LLMService llmService;

    public CustomBotController(LLMService llmService) {
        this.llmService = llmService;
    }

    @GetMapping("/get-answer")
    public ResponseEntity<String> getAnswerConvModel(@RequestParam("question") String question){
        String contentConvModel = llmService.getAnswer(question);
        return new ResponseEntity<>(contentConvModel, OK);
    }

}
