package org.example.pharmaproject.bot.handlers;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.Category;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.CategoryService;
import org.example.pharmaproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
public class MenuHandler {

    private final CategoryService categoryService;
    private final UserService userService;

    @Autowired
    public MenuHandler(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    /**
     * Asosiy menyuni ko‘rsatish
     */
    public BotApiMethod<?> handleMenu(Message message, User user) {
        String chatId = message.getChatId().toString();

        List<Category> categories = categoryService.findAll();
        String text = BotUtils.getLocalizedMessage(user.getLanguage(), "menu_message");

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.createCategoryInlineKeyboard(categories, user.getLanguage()));
        return response;
    }

    /**
     * Kategoriya tanlash
     */
    public BotApiMethod<?> handleCategorySelection(CallbackQuery query, String categoryId) {
        String chatId = query.getMessage().getChatId().toString();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        Category category = categoryService.findById(Long.parseLong(categoryId))
                .orElseThrow(() -> new RuntimeException("Kategoriya topilmadi"));

        String text = BotUtils.getLocalizedMessage(user.getLanguage(), "category_selected") + category.getName();

        if (category.getProducts().isEmpty()) {
            text += "\n\n" + BotUtils.getLocalizedMessage(user.getLanguage(), "no_products");
            SendMessage response = new SendMessage(chatId, text);
            response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
            return response;
        } else {
            text += "\n\n" + BotUtils.getLocalizedMessage(user.getLanguage(), "products_list");
            for (Product product : category.getProducts()) {
                text += String.format("\n\n💊 %s\n💵 %s so‘m", product.getName(), String.format("%,.0f", product.getPrice()));
            }
            SendMessage response = new SendMessage(chatId, text);
            response.setReplyMarkup(BotUtils.createProductsInlineKeyboard(category.getProducts(), user.getLanguage()));
            return response;
        }
    }
}