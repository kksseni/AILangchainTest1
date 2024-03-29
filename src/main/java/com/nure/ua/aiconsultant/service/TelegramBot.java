package com.nure.ua.aiconsultant.service;


import com.nure.ua.aiconsultant.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig bot;
    private final LLMService llmService;

    static final String INTRO_TEXT = """
                                    Привіт, %s, радій зустріти тебе!
                                    Я консультант абітурієнтів ХНУРЕ.
                                    Яке питання тебе цікавить?
                                    """;

    public TelegramBot(BotConfig bot, LLMService llmService) {
        this.bot = bot;
        this.llmService = llmService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String answer;
                    switch (messageText) {
                case "/start":
                    answer = String.format(INTRO_TEXT, update.getMessage().getChat().getFirstName());
                    log.info("Replied to user " + update.getMessage().getChat().getFirstName());
                    break;

                default:
                    answer = llmService.getContentConvModel(update.getMessage().getText());
            }
            sendMessage(chatId, answer);
        }
    }
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

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
}
