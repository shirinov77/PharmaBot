package org.example.pharmaproject.bot.handlers;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.*;
import org.example.pharmaproject.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class StartHandler {

    @Autowired
    private UserService userService;

    public BotApiMethod<?> handleStart(Message message, User user) {
        String chatId = message.getChatId().toString();

        String text = getLocalizedMessage(user.getLanguage(), "welcome_message");

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.createLanguageInlineKeyboard());
        return response;
    }

    public BotApiMethod<?> handleLanguageSelection(Message message, User user) {
        String chatId = message.getChatId().toString();

        String text = getLocalizedMessage(user.getLanguage(), "select_language");

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.createLanguageInlineKeyboard());
        return response;
    }

    public BotApiMethod<?> handleLanguageChange(CallbackQuery query, String lang) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        user.setLanguage(lang);
        userService.save(user);

        String text = getLocalizedMessage(lang, "language_changed");

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.getMainKeyboard(lang));
        return response;
    }

    private String getLocalizedMessage(String lang, String key) {
        switch (key) {
            case "welcome_message":
                return switch (lang) {
                    case "uz" -> "Salom 👋 Apteka botiga xush kelibsiz!\n\nIltimos, tilni tanlang:";
                    case "ru" -> "Здравствуйте 👋 Добро пожаловать в бот аптеки!\n\nПожалуйста, выберите язык:";
                    case "en" -> "Hello 👋 Welcome to the Pharmacy Bot!\n\nPlease select a language:";
                    default -> "Welcome!";
                };
            case "select_language":
                return switch (lang) {
                    case "uz" -> "Iltimos, tilni tanlang:";
                    case "ru" -> "Пожалуйста, выберите язык:";
                    case "en" -> "Please select a language:";
                    default -> "Select language:";
                };
            case "language_changed":
                return switch (lang) {
                    case "uz" -> "Til o‘zgartirildi! Endi asosiy menyudan foydalaning.";
                    case "ru" -> "Язык изменён! Используйте главное меню.";
                    case "en" -> "Language changed! Use the main menu.";
                    default -> "Language changed!";
                };
            default:
                return "Unknown message";
        }
    }
}