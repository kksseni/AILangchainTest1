package com.nure.ua.aiconsultant.controller;

import com.nure.ua.aiconsultant.dto.ChatGPTRequest;
import com.nure.ua.aiconsultant.dto.ChatGptResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/bot")
public class CustomBotController {

    @Value("${openai.model}")
    private String model;

    @Value(("${openai.api.url}"))
    private String apiURL;

    @Autowired
    private RestTemplate template;

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
}
