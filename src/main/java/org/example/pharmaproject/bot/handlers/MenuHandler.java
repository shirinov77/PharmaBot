package org.example.pharmaproject.bot.handlers;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.Category;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class MenuHandler {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    public BotApiMethod<?> handleMenu(Message message, User user) {
        String chatId = message.getChatId().toString();

        String text = getLocalizedMessage(user.getLanguage(), "menu_message");

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.createCategoryInlineKeyboard(categoryService.findAll(), user.getLanguage()));
        response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        return response;
    }

    public BotApiMethod<?> handleCategorySelection(CallbackQuery query, String categoryId) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        Category category = categoryService.findById(Long.parseLong(categoryId))
                .orElseThrow(() -> new RuntimeException("Kategoriya topilmadi"));

        String text = getLocalizedMessage(user.getLanguage(), "category_selected") + category.getName();

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.createProductsInlineKeyboard(category.getProducts(), user.getLanguage()));
        return response;
    }

    private String getLocalizedMessage(String lang, String key) {
        switch (key) {
            case "menu_message":
                return switch (lang) {
                    case "uz" -> "📋 Menyu:\nKategoriyalarni tanlang:";
                    case "ru" -> "📋 Меню:\nВыберите категорию:";
                    case "en" -> "📋 Menu:\nSelect a category:";
                    default -> "Select a category";
                };
            case "category_selected":
                return switch (lang) {
                    case "uz" -> "Tanlangan kategoriya: ";
                    case "ru" -> "Выбранная категория: ";
                    case "en" -> "Selected category: ";
                    default -> "Selected category: ";
                };
            default:
                return "Unknown message";
        }
    }
}