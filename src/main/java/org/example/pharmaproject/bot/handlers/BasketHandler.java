package org.example.pharmaproject.bot.handlers;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.BasketService;
import org.example.pharmaproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class BasketHandler {

    private final BasketService basketService;
    private final UserService userService;

    @Autowired
    public BasketHandler(BasketService basketService, UserService userService) {
        this.basketService = basketService;
        this.userService = userService;
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

        EditMessageText response = new EditMessageText();
        response.setChatId(chatId);
        response.setMessageId(message.getMessageId());
        response.setText(text);
        response.setReplyMarkup(BotUtils.createBasketManagementKeyboard(basket, user.getLanguage()));
        return response;
    }

    /**
     * Savatga qo‘shish
     */
    public BotApiMethod<?> handleAddToBasket(CallbackQuery query, String productId) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        basketService.addToBasket(user, Long.parseLong(productId));

        Basket updatedBasket = basketService.getBasketByUser(user);
        String updatedText = getBasketSummary(updatedBasket, user.getLanguage());

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(updatedText);
        editMessage.setReplyMarkup(BotUtils.createBasketManagementKeyboard(updatedBasket, user.getLanguage()));
        return editMessage;
    }

    /**
     * Savatdan mahsulotni o'chirish
     */
    public BotApiMethod<?> handleRemoveFromBasket(CallbackQuery query, String productId) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        basketService.removeFromBasket(user, Long.parseLong(productId));

        Basket updatedBasket = basketService.getBasketByUser(user);
        String updatedText = updatedBasket.getProducts().isEmpty()
                ? BotUtils.getLocalizedMessage(user.getLanguage(), "empty_basket")
                : getBasketSummary(updatedBasket, user.getLanguage());

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(updatedText);
        editMessage.setReplyMarkup(BotUtils.createBasketManagementKeyboard(updatedBasket, user.getLanguage()));
        return editMessage;
    }

    /**
     * Savatni tozalash
     */
    public BotApiMethod<?> handleClearBasket(CallbackQuery query) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();

        Long telegramId = query.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException("Foydalanuvchi topilmadi: " + telegramId));

        basketService.clearBasket(user);

        String language = user.getLanguage() != null ? user.getLanguage() : "uz";
        String clearedMessage = BotUtils.getLocalizedMessage(language, "basket_cleared");

        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(clearedMessage)
                .replyMarkup(BotUtils.createBackToMenuKeyboard(language))
                .build();
    }


    /**
     * Savat haqida umumiy ma'lumot matnini yaratish
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
