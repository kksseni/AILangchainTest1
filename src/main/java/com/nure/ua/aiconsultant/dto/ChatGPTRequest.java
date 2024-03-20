package com.nure.ua.aiconsultant.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatGPTRequest {

    private String model;
    private List<Message> messages;

    public ChatGPTRequest(String model, String question, String context) {
        this.model = model;
        this.messages = new ArrayList<>();
        String prompt = "Ти консультант для абітурієнтів у чаті на сайті кафедри Інформатики Харківського Університету Радіоелектроніки, яка навчає студентів за спеціальністю Комп'ютерні науки, освітньо-професійна програма Інформатика. У тебе є інформація про спеціальність Комп'ютерні науки, освітньо-професійна програма Інформатика, також про саму кафедру Інформатика, яка є провідною для програми Інформатика, її наукову діяльність та інші питання. Тобі ставить запитання абітурієнт у чаті, дай йому відповідь, спираючись на документ, відповідай максимально точно за документом, не вигадуй нічого від себе. Не згадуй документ під час відповіді, абітурієнт нічого не повинен знати про документ, за яким ти відповідаєш. Відповідай так, щоб абітурієнт захотів вступити на спеціальність Комп'ютерні науки, освітньо-професійна програма Інформатика. Дай відповідь не більше, ніж на 3000 знаків. \n" +
                "Документ з інформацією для відповіді клієнту:";
        this.messages.add(new Message("system", prompt + context));
        this.messages.add(new Message("user",question));
    }
}
