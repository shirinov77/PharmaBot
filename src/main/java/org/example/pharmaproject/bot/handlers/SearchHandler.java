package org.example.pharmaproject.bot.handlers;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.ProductService;
import org.example.pharmaproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
public class SearchHandler {

    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public SearchHandler(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    /**
     * Foydalanuvchidan kelgan qidiruv so'rovini qayta ishlash
     */
    public BotApiMethod<?> handleSearch(Message message, String query, User user) {
        String chatId = message.getChatId().toString();

        List<Product> products = productService.searchByName(query);

        if (products.isEmpty()) {
            String text = BotUtils.getLocalizedMessage(user.getLanguage(), "no_results");
            SendMessage response = new SendMessage(chatId, text);
            response.setReplyMarkup(BotUtils.createBackToMenuKeyboard(user.getLanguage()));
            return response;
        } else {
            String text = BotUtils.getLocalizedMessage(user.getLanguage(), "search_results")
                    + " (" + products.size() + ")";
            SendMessage response = new SendMessage(chatId, text);
            response.setReplyMarkup(BotUtils.createProductsInlineKeyboard(products, user.getLanguage()));
            return response;
        }
    }

    /**
     * Mahsulot tafsilotlarini ko'rsatish
     */
    public SendPhoto handleProductDetails(CallbackQuery query, String productId) {
        String chatId = query.getMessage().getChatId().toString();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        Product product = productService.findById(Long.parseLong(productId))
                .orElseThrow(() -> new RuntimeException("Mahsulot topilmadi"));

        // Caption matni
        String caption = String.format(
                BotUtils.getLocalizedMessage(user.getLanguage(), "product_details"),
                product.getName(),
                String.format("%,.0f so‘m", product.getPrice()),
                product.getQuantity()
        );

        // Agar imageUrl bo'sh bo'lsa, default rasmi yuboriladi
        InputFile photoFile;
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            photoFile = new InputFile(product.getImageUrl());
        } else {
            photoFile = new InputFile(getClass().getResourceAsStream("/static/no-image.png"), "no-image.png");
        }

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(photoFile);
        photo.setCaption(caption);
        photo.setReplyMarkup(BotUtils.createProductDetailsInline(productId, user.getLanguage()));

        return photo;
    }

    /**
     * Foydalanuvchiga qidiruv uchun prompt yuborish
     */
    public SendMessage handleSearchPrompt(Message message, User user) {
        String chatId = message.getChatId().toString();
        String promptText = BotUtils.getLocalizedMessage(user.getLanguage(), "enter_search_query");
        SendMessage response = new SendMessage(chatId, promptText);
        response.setReplyMarkup(BotUtils.createBackToMenuKeyboard(user.getLanguage()));
        return response;
    }
}
