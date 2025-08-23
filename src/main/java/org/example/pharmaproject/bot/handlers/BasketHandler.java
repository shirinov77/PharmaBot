package org.example.pharmaproject.bot.handlers;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.BasketService;
import org.example.pharmaproject.services.ProductService;
import org.example.pharmaproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.Optional;

@Component
public class BasketHandler {

    private final BasketService basketService;
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public BasketHandler(BasketService basketService, UserService userService, ProductService productService) {
        this.basketService = basketService;
        this.userService = userService;
        this.productService = productService;
    }

    /**
     * Savatni ko‘rsatish
     */
    public BotApiMethod<?> handleBasket(Message message, User user) {
        String chatId = message.getChatId().toString();

        Basket basket = basketService.getBasketByUser(user);
        String text = basket.getProducts().isEmpty()
                ? BotUtils.getLocalizedMessage(user.getLanguage(), "empty_basket")
                : getBasketSummary(basket, user.getLanguage());

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.createBasketManagementKeyboard(basket, user.getLanguage()));
        return response;
    }

    /**
     * Callback so‘rovi orqali savatni ko‘rsatish
     */
    public BotApiMethod<?> handleBasketCallback(CallbackQuery query, User user) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();

        Basket basket = basketService.getBasketByUser(user);
        String text = getBasketSummary(basket, user.getLanguage());

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setReplyMarkup(BotUtils.createBasketManagementKeyboard(basket, user.getLanguage()));
        return editMessage;
    }

    /**
     * Mahsulotni savatga qo‘shish
     */
    public BotApiMethod<?> handleAddToBasket(CallbackQuery query, String productId) {
        String chatId = query.getMessage().getChatId().toString();
        User user = userService.findByTelegramId(query.getFrom().getId()).orElseThrow();

        try {
            Product product = productService.findById(Long.parseLong(productId))
                    .orElseThrow(() -> new IllegalArgumentException("Mahsulot topilmadi: " + productId));

            basketService.addProductToBasket(user, product);

            return EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(query.getMessage().getMessageId())
                    .text(BotUtils.getLocalizedMessage(user.getLanguage(), "product_added_to_basket"))
                    .replyMarkup(BotUtils.createBackToMenuKeyboard(user.getLanguage()))
                    .build();

        } catch (Exception e) {
            return EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(query.getMessage().getMessageId())
                    .text("❌ " + e.getMessage())
                    .build();
        }
    }

    /**
     * Savatdan mahsulotni o‘chirish
     */
    public BotApiMethod<?> handleRemoveFromBasket(CallbackQuery query, String productId) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();
        User user = userService.findByTelegramId(query.getFrom().getId()).orElseThrow();

        basketService.removeProductFromBasket(user, Long.parseLong(productId));

        Basket basket = basketService.getBasketByUser(user);
        String text = basket.getProducts().isEmpty()
                ? BotUtils.getLocalizedMessage(user.getLanguage(), "empty_basket")
                : getBasketSummary(basket, user.getLanguage());

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setReplyMarkup(BotUtils.createBasketManagementKeyboard(basket, user.getLanguage()));
        return editMessage;
    }

    /**
     * Savatni tozalash
     */
    public BotApiMethod<?> handleClearBasket(CallbackQuery query) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();
        User user = userService.findByTelegramId(query.getFrom().getId()).orElseThrow();

        basketService.clearBasket(user);

        String clearedMessage = BotUtils.getLocalizedMessage(user.getLanguage(), "basket_cleared");

        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(clearedMessage)
                .replyMarkup(BotUtils.createBackToMenuKeyboard(user.getLanguage()))
                .build();
    }

    /**
     * Savat haqida umumiy ma'lumot matnini yaratish
     * Har bir mahsulotdan bir dona hisoblanadi.
     */
    private String getBasketSummary(Basket basket, String lang) {
        StringBuilder summary = new StringBuilder(BotUtils.getLocalizedMessage(lang, "basket_summary"));
        double total = 0.0;

        for (Product product : basket.getProducts()) {
            summary.append("\n\n💊 ").append(product.getName())
                    .append("\n💵 ").append(String.format("%,.0f so‘m", product.getPrice()));
            total += product.getPrice();
        }

        summary.append("\n\n💰 ")
                .append(String.format(BotUtils.getLocalizedMessage(lang, "total_price"), String.format("%,.0f so‘m", total)));

        return summary.toString();
    }
}

