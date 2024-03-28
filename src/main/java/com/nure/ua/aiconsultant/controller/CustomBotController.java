package com.nure.ua.aiconsultant.controller;

import com.google.gson.Gson;
import com.nure.ua.aiconsultant.dto.ChatGPTRequest;
import com.nure.ua.aiconsultant.dto.ChatGptResponse;
import com.nure.ua.aiconsultant.service.LLMService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/bot")
public class CustomBotController {

    @Value("${openai.model}")
    private String model;

    @Value(("${openai.api.url}"))
    private String apiURL;

    private final RestTemplate template;

    private final LLMService llmService;

    public CustomBotController(RestTemplate template, LLMService llmService) {
        this.template = template;
        this.llmService = llmService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam("question") String question){
        String prompt = "Основними напрямками освітньої програми Інформатика є:\n" +
                "- сучасні технології та мови програмування;\n" +
                "- розробка інформаційних та інтелектуальних систем;\n" +
                "- математичне та комп’ютерне моделювання процесів і\n" +
                "систем різної природи.\n" +
                "\n" +
                "Основна інформація про освітню програму:\n" +
                " Ключові навчальні дисципліни\n" +
                "Hard Skills\n" +
                "Професії випускника ОП\n" +
                "\n" +
                "Особлива увага приділяється поєднанню математичної підготовки студентів зі знаннями в області інформаційних технологій, а також профільному напрямку: обробки і аналізу складних багатовимірних даних.\n" +
                " Ключові навчальні дисципліни\n" +
                "\n" +
                "1 курс:\n" +
                "Об'єктно-орієнтоване програмування\n" +
                "Дискретна математика\n" +
                "Математичний аналіз\n" +
                "2 курс:\n" +
                "Бази даних та інформаційні системи\n" +
                "Кросплатформне програмування\n" +
                "Web-технології та web-дизайн\n" +
                "3 курс:\n" +
                "Інтелектуальний аналіз даних\n" +
                "Програмування JavaScript\n" +
                "Нереляційні бази даних NoSQL\n" +
                "Машинне навчання\n" +
                "4 курс:\n" +
                "Технології розподілених систем\n" +
                "та паралельних обчислень\n" +
                "Обробка зображень та мультимедіа\n" +
                "Методи аналізу зображень та відео\n";
        ChatGPTRequest request=new ChatGPTRequest(model, question, prompt);
        ChatGptResponse chatGptResponse = template.postForObject(apiURL, request, ChatGptResponse.class);
        return chatGptResponse.getChoices().get(0).getMessage().getContent();
    }
    @GetMapping("/get-answer")
    public ResponseEntity<String> getAnswerConvModel(@RequestParam("question") String question){
        String contentConvModel = llmService.getContentConvModel(question);
        return new ResponseEntity<>(contentConvModel, OK);
    }

    @GetMapping("/get-answer-fragments")
    public ResponseEntity<List<String>> getAnswerFragments(@RequestParam("question") String question){
        List<EmbeddingMatch<TextSegment>> contentFromDoc = llmService.getContentFromDoc(question);
        contentFromDoc.forEach(content->{
            System.out.println(new Gson().toJson(content));
        });


        List<String> stringList = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : contentFromDoc) {
            TextSegment textSegment = match.embedded();
            String text = textSegment.text();
            stringList.add(text);
        }


        return new ResponseEntity<>(stringList, OK);
    }

}
