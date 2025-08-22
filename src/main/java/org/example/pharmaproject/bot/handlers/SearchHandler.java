package org.example.pharmaproject.bot.handlers;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.*;
import org.example.pharmaproject.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.util.List;

@Component
public class SearchHandler {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    /**
     * Foydalanuvchidan kelgan qidiruv so'rovini qayta ishlash
     */
    public BotApiMethod<Message> handleSearch(Message message, String query, User user) {
        String chatId = message.getChatId().toString();

        List<Product> products = productService.searchByName(query);
        String text;
        if (products.isEmpty()) {
            text = getLocalizedMessage(user.getLanguage(), "no_results");
        } else {
            text = getLocalizedMessage(user.getLanguage(), "search_results") + products.size();
        }

        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        response.setText(text);

        if (!products.isEmpty()) {
            response.setReplyMarkup(BotUtils.createProductsInlineKeyboard(products, user.getLanguage()));
        } else {
            response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        }

        return response;
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

        String caption = String.format(
                getLocalizedMessage(user.getLanguage(), "product_details"),
                product.getName(), product.getPrice(), product.getQuantity()
        );

        InputFile photoFile;
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            photoFile = new InputFile(product.getImageUrl());
        } else {
            photoFile = new InputFile("https://example.com/default_image.jpg"); // default rasm
        }

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(photoFile);
        photo.setCaption(caption);
        photo.setReplyMarkup(BotUtils.createAddToBasketInline(productId, user.getLanguage()));

        return photo;
    }

    /**
     * Til bo‘yicha xabar matnlarini qaytaruvchi yordamchi metod
     */
    private String getLocalizedMessage(String lang, String key) {
        switch (key) {
            case "search_results":
                return switch (lang) {
                    case "uz" -> "🔍 Qidiruv natijalari: ";
                    case "ru" -> "🔍 Результаты поиска: ";
                    case "en" -> "🔍 Search results: ";
                    default -> "Search results: ";
                };
            case "no_results":
                return switch (lang) {
                    case "uz" -> "❌ Hech narsa topilmadi.";
                    case "ru" -> "❌ Ничего не найдено.";
                    case "en" -> "❌ No results found.";
                    default -> "No results found.";
                };
            case "product_details":
                return switch (lang) {
                    case "uz" -> "Mahsulot: %s\nNarxi: %s\nMiqdori: %d";
                    case "ru" -> "Товар: %s\nЦена: %s\nКоличество: %d";
                    case "en" -> "Product: %s\nPrice: %s\nQuantity: %d";
                    default -> "Product: %s\nPrice: %s\nQuantity: %d";
                };
            default:
                return "Unknown message";
        }
    }
}
