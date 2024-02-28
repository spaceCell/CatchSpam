package com.example.demo.model;

import com.example.demo.config.BotConfig;
import com.example.demo.service.PatternLoaderService;
import com.example.demo.service.SpamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;

@Component
@Slf4j
public class BotRmDel extends TelegramLongPollingBot {
    private static final String CHAT_ID = "-1002131436559";
    @Autowired
    private BotConfig botConfig;
    @Autowired
    private SpamService spamService;

    @Autowired
    private PatternLoaderService patternLoaderService;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String message = update.getMessage().getText();
            String userName = update.getMessage().getFrom().getUserName();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            Long userId = update.getMessage().getFrom().getId();
            String senderName = userName + " " + firstName + " " + lastName;

            log.info("Received message in chatId {}: {}", chatId, message);

            boolean isSpam = spamService.isSpam(message);

            if (isSpam) {
                log.info("Message identified as spam: {}", message);
                deleteAndSendToBotChat(chatId, update.getMessage().getMessageId(), message, senderName, userId);
            }
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String message = update.getMessage().getText();

            if ("/visaend".equals(message)) {
                requestVisaEndParameters(chatId);
            } else {
                processVisaEndParameters(chatId, message);
            }
        }
    }

    private void requestVisaEndParameters(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Please enter the start date and the number of days separated by space, e.g., '2024-01-12 30'");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error requesting visa end parameters: {}", e.getMessage());
        }
    }

    private void processVisaEndParameters(String chatId, String message) {
        String[] parts = message.split(" ");
        if (parts.length != 2) {
            sendErrorMessage(chatId, "Invalid input format. Please provide the start date and the number of days separated by space.");
            return;
        }

        LocalDate startDate;
        int countDays;
        try {
            startDate = LocalDate.parse(parts[0]);
            countDays = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            sendErrorMessage(chatId, "Invalid input format. Please provide a valid start date in the format 'yyyy-MM-dd' and a valid number of days.");
            return;
        }

        LocalDate endDate = countDayInCountry(startDate, countDays);
        sendVisaEndResponse(chatId, endDate);
    }

    private LocalDate countDayInCountry(LocalDate start, int countDay) {
        int tmp = countDay - 1;
        return start.plusDays(tmp);
    }

    private void sendVisaEndResponse(String chatId, LocalDate endDate) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Your visa will end on: " + endDate);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending visa end response: {}", e.getMessage());
        }
    }

    private void sendErrorMessage(String chatId, String errorMessage) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(errorMessage);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending error message: {}", e.getMessage());
        }
    }

    private void deleteAndSendToBotChat(String chatId, Integer messageId, String messageText, String senderName,
                                        Long userId) {
        DeleteMessage deleteMessage = new DeleteMessage(chatId, messageId);

        String notificationChatId = CHAT_ID;

        SendMessage sendMessage = new SendMessage(notificationChatId, "Deleted Spam. Username: " +
                senderName + ":\n" + messageText);

        BanChatMember banChatMember = BanChatMember.builder()
                .chatId(chatId)
                .userId(userId)
                .build();

        try {
            execute(deleteMessage);
            execute(sendMessage);
            execute(banChatMember);
        } catch (TelegramApiException e) {
            log.error("Error deleting or sending message: {}", e.getMessage());
        }
    }
}
