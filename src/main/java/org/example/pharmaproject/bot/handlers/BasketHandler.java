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

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public BotApiMethod<?> handleAddToBasket(CallbackQuery query, String productId) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();
        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi."));

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);

        try {
            Product product = productService.findById(Long.parseLong(productId))
                    .orElseThrow(() -> new IllegalArgumentException("Mahsulot topilmadi: " + productId));
            basketService.addOrIncreaseProductInBasket(user, product);

            editMessage.setText(BotUtils.getLocalizedMessage(user.getLanguage(), "product_added_to_basket"));
            editMessage.setReplyMarkup(BotUtils.createBackToMenuKeyboard(user.getLanguage()));

        } catch (Exception e) {
            editMessage.setText("❌ " + e.getMessage());
        }

        return editMessage;
    }

    public BotApiMethod<?> handleRemoveFromBasket(CallbackQuery query, String productId) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();
        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi."));

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

    public BotApiMethod<?> handleIncreaseProductCount(CallbackQuery query, String productId) {
        return updateProductQuantity(query, productId, true);
    }

    public BotApiMethod<?> handleDecreaseProductCount(CallbackQuery query, String productId) {
        return updateProductQuantity(query, productId, false);
    }

    private BotApiMethod<?> updateProductQuantity(CallbackQuery query, String productId, boolean increase) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();
        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi."));

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);

        try {
            Long pid = Long.parseLong(productId);
            if (increase) {
                basketService.increaseProductQuantityInBasket(user, pid);
            } else {
                basketService.decreaseProductQuantityInBasket(user, pid);
            }

            Basket basket = basketService.getBasketByUser(user);
            String text = basket.getProducts().isEmpty()
                    ? BotUtils.getLocalizedMessage(user.getLanguage(), "empty_basket")
                    : getBasketSummary(basket, user.getLanguage());

            editMessage.setText(text);
            editMessage.setReplyMarkup(BotUtils.createBasketManagementKeyboard(basket, user.getLanguage()));

        } catch (Exception e) {
            editMessage.setText("❌ " + e.getMessage());
        }

        return editMessage;
    }

    public BotApiMethod<?> handleClearBasket(CallbackQuery query) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();
        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi."));

        basketService.clearBasket(user);

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(BotUtils.getLocalizedMessage(user.getLanguage(), "basket_cleared"));
        editMessage.setReplyMarkup(BotUtils.createBackToMenuKeyboard(user.getLanguage()));
        return editMessage;
    }

    private String getBasketSummary(Basket basket, String lang) {
        StringBuilder summary = new StringBuilder(BotUtils.getLocalizedMessage(lang, "basket_summary"));
        double total = 0.0;

        Map<Product, Long> productCounts = basket.getProducts().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        for (Map.Entry<Product, Long> entry : productCounts.entrySet()) {
            Product product = entry.getKey();
            Long count = entry.getValue();
            double productTotal = product.getPrice() * count;

            summary.append("\n\n💊 ").append(product.getName())
                    .append("\n🔢 ").append(count)
                    .append(" x ").append(String.format("%,.0f so‘m", product.getPrice()))
                    .append(" = ").append(String.format("%,.0f so‘m", productTotal));

            total += productTotal;
        }

        summary.append("\n\n💰 ")
                .append(String.format(BotUtils.getLocalizedMessage(lang, "total_price"), String.format("%,.0f so‘m", total)));

        return summary.toString();
    }
}
