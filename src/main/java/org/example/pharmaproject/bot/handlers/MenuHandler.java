package org.example.pharmaproject.bot.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.Category;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.CategoryService;
import org.example.pharmaproject.services.ProductService;
import org.example.pharmaproject.services.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MenuHandler {

    private final CategoryService categoryService;
    private final UserService userService;
    private final ProductService productService;

    public BotApiMethod<?> handleMenu(Message message, User user) {
        String chatId = message.getChatId().toString();
        List<Category> categories = categoryService.findAll();
        String text = BotUtils.getLocalizedMessage(user.getLanguage(), "menu_message");

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.createCategoryInlineKeyboard(categories, user.getLanguage()));
        return response;
    }

    public BotApiMethod<?> handleCategorySelection(CallbackQuery query, String categoryId) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        Category category = categoryService.findById(Long.parseLong(categoryId))
                .orElseThrow(() -> new RuntimeException("Kategoriya topilmadi"));

        List<Product> products = category.getProducts();
        StringBuilder textBuilder = new StringBuilder(
                BotUtils.getLocalizedMessage(user.getLanguage(), "category_selected") + " " + category.getName()
        );

        if (products.isEmpty()) {
            textBuilder.append("\n\n").append(BotUtils.getLocalizedMessage(user.getLanguage(), "no_products"));
            return EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(textBuilder.toString())
                    .replyMarkup(BotUtils.createBackToMenuKeyboard(user.getLanguage()))
                    .build();
        } else {
            textBuilder.append("\n\n").append(BotUtils.getLocalizedMessage(user.getLanguage(), "products_list"));
            for (Product product : products) {
                textBuilder.append(String.format("\n\n💊 %s\n💵 %s so‘m", product.getName(), String.format("%,.0f", product.getPrice())));
            }

            return EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(textBuilder.toString())
                    .replyMarkup(BotUtils.createProductsInlineKeyboard(products, user.getLanguage()))
                    .build();
        }
    }

    public BotApiMethod<?> handleProductDetails(CallbackQuery query, String productId) {
        String chatId = query.getMessage().getChatId().toString();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        Product product = productService.findById(Long.parseLong(productId))
                .orElseThrow(() -> new RuntimeException("Mahsulot topilmadi"));

        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append(String.format(
                BotUtils.getLocalizedMessage(user.getLanguage(), "product_details"),
                product.getName(),
                String.format("%,.0f so‘m", product.getPrice()),
                product.getQuantity()
        ));

        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            textBuilder.append("\n\n").append(product.getDescription());
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textBuilder.toString());
        message.setParseMode("HTML");
        message.setReplyMarkup(BotUtils.createProductDetailsInline(productId, user.getLanguage()));

        return message;
    }
}