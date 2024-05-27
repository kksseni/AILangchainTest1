package com.nure.ua.aiconsultant.service;


import com.nure.ua.aiconsultant.config.BotConfig;
import com.nure.ua.aiconsultant.domain.Report;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig bot;
    private final LLMService llmService;
    private final AnalyticsService analyticsService;

    static final String INTRO_TEXT = """
                                    Привіт, %s, радій зустріти тебе!
                                    Я консультант абітурієнтів ХНУРЕ.
                                    Обери потрібну опцію нижче.
                                    """;

    static final String QUESTION_TEXT = """
                                    Яке питання тебе цікавить?
                                    """;
    static final String LIMIT_TEXT = """
                                    На жаль, ви вичерпали свій ліміт питань на годину.
                                    Спробуйте задати питання пізніше.
                                    """;

    public TelegramBot(BotConfig bot, LLMService llmService, AnalyticsService analyticsService) {
        this.bot = bot;
        this.llmService = llmService;
        this.analyticsService = analyticsService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId;
        String answer;
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
             chatId = update.getMessage().getChatId();
            if (messageText.equals("/start")) {
                answer = String.format(INTRO_TEXT, update.getMessage().getChat().getFirstName());
                log.info("Replied to user " + update.getMessage().getChat().getFirstName());
                sendMessage(chatId, answer, createInlineKeyboard());
            } else if (isLimit(chatId)){
                answer = LIMIT_TEXT;
                sendMessage(chatId, answer, null);
            }
            else {
                answer = llmService.getAnswer(update.getMessage().getText());
                saveReport(update, answer, chatId);
                sendMessage(chatId, answer, null);
                sendMessage(chatId, "Оцініть відповідь бота:", createRatingKeyboard());
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            chatId = update.getCallbackQuery().getMessage().getChatId();
            switch (callbackData) {
                case "ask_question":
                    if (isLimit(chatId)){
                        answer = LIMIT_TEXT;
                        sendMessage(chatId, answer, null);
                        break;//????????????????
                    }
                    answer = QUESTION_TEXT;
                    sendMessage(chatId, answer, null);
                    break;
                case "get_average_rating":
                    answer = "На даний момент середня оцінка бота - " + analyticsService.getAvgRate()+" .";
                    answer += "\nМи працюємо над його вдосконаленням.";
                    sendMessage(chatId, answer, createInlineKeyboard());
                    break;
                default:
                    if (callbackData.startsWith("rate_")) {
                        answer = setReportRate(chatId, callbackData);
                        sendMessage(chatId, answer, createInlineKeyboard());
                    }
                    break;
            }
        }
    }

    @NotNull
    private String setReportRate(Long chatId, String callbackData) {
        String answer;
        Report prevByChatId = analyticsService.getPrevByChatId(chatId);
        String ratingString = callbackData.substring("rate_".length());
        try {
            answer ="Дякую!";
            Integer rating = Integer.parseInt(ratingString);
            prevByChatId.setReportRate(rating);
            analyticsService.update(prevByChatId);
        } catch (NumberFormatException e) {
            answer = "Щось пішло не так((";
        }
        return answer;
    }

    private boolean isLimit(Long userId){
        Long numOfMessagesPerHour = analyticsService.getNumOfMessagesPerHour(userId);
        return numOfMessagesPerHour>=20;
    }
    private void saveReport(Update update, String answer, Long chatId) {
        Report report = Report.builder()
                .reportDate(LocalDateTime.now())
                .question(update.getMessage().getText())
                .answer(answer)
                .userId(chatId)
                .build();
        analyticsService.insert(report);
    }

    private void sendMessage(long chatId, String textToSend, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setReplyMarkup(markup);

        executeMessage(message);
    }
    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("ERROR_TEXT" + e.getMessage());
        }
    }
    public String getBotUsername() {
        return this.bot.getBotName();
    }

    public String getBotToken() {
        return this.bot.getToken();
    }

    private InlineKeyboardMarkup createRatingKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> ratingButtons = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            InlineKeyboardButton ratingButton = new InlineKeyboardButton();
            ratingButton.setText(String.valueOf(i));
            ratingButton.setCallbackData("rate_" + i);
            ratingButtons.add(ratingButton);
        }

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(ratingButtons);

        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup createInlineKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton questionButton = new InlineKeyboardButton();
        questionButton.setText("Задати питання");
        questionButton.setCallbackData("ask_question");

        InlineKeyboardButton getAverageRatingButton = new InlineKeyboardButton();
        getAverageRatingButton.setText("Середня оцінка бота");
        getAverageRatingButton.setCallbackData("get_average_rating");

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(questionButton);

        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow2.add(getAverageRatingButton);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);

        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }
}
