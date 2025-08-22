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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class BasketHandler {

    @Autowired
    private BasketService basketService;

    @Autowired
    private UserService userService;

    public BotApiMethod<?> handleBasket(Message message, User user) {
        String chatId = message.getChatId().toString();

        Basket basket = basketService.getBasketByUser(user);
        String text = basket.getProducts().isEmpty()
                ? getLocalizedMessage(user.getLanguage(), "empty_basket")
                : getBasketSummary(basket, user.getLanguage());

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.createBasketInlineKeyboard(basket.getProducts(), user.getLanguage()));
        response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        return response;
    }

    public BotApiMethod<?> handleAddToBasket(CallbackQuery query, String productId) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        Product product = basketService.addToBasket(user, Long.parseLong(productId));

        String text = getLocalizedMessage(user.getLanguage(), "added_to_basket") + product.getName();

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        return response;
    }

    public BotApiMethod<?> handleRemoveFromBasket(CallbackQuery query, String productId) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        basketService.removeFromBasket(user, Long.parseLong(productId));

        String text = getLocalizedMessage(user.getLanguage(), "removed_from_basket");

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        return response;
    }

    public BotApiMethod<?> handleClearBasket(CallbackQuery query) {
        String chatId = query.getMessage().getChatId().toString();
        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        basketService.clearBasket(user);

        String text = getLocalizedMessage(user.getLanguage(), "basket_cleared");

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        return response;
    }

    private String getBasketSummary(Basket basket, String lang) {
        StringBuilder summary = new StringBuilder(getLocalizedMessage(lang, "basket_summary"));
        double total = 0;
        for (Product product : basket.getProducts()) {
            summary.append(String.format("\n- %s: %s", product.getName(), product.getPrice()));
            total += product.getPrice();
        }
        summary.append(String.format(getLocalizedMessage(lang, "total_price"), total));
        return summary.toString();
    }

    private String getLocalizedMessage(String lang, String key) {
        switch (key) {
            case "basket_summary":
                return switch (lang) {
                    case "uz" -> "🛒 Savatingiz:";
                    case "ru" -> "🛒 Ваша корзина:";
                    case "en" -> "🛒 Your basket:";
                    default -> "Your basket:";
                };
            case "empty_basket":
                return switch (lang) {
                    case "uz" -> "🛒 Savatingiz bo‘sh.";
                    case "ru" -> "🛒 Ваша корзина пуста.";
                    case "en" -> "🛒 Your basket is empty.";
                    default -> "Your basket is empty.";
                };
            case "added_to_basket":
                return switch (lang) {
                    case "uz" -> "✅ Savatga qo‘shildi: ";
                    case "ru" -> "✅ Добавлено в корзину: ";
                    case "en" -> "✅ Added to basket: ";
                    default -> "Added to basket: ";
                };
            case "removed_from_basket":
                return switch (lang) {
                    case "uz" -> "🗑 Savatdan o‘chirildi.";
                    case "ru" -> "🗑 Удалено из корзины.";
                    case "en" -> "🗑 Removed from basket.";
                    default -> "Removed from basket.";
                };
            case "basket_cleared":
                return switch (lang) {
                    case "uz" -> "🛒 Savat tozalandi.";
                    case "ru" -> "🛒 Корзина очищена.";
                    case "en" -> "🛒 Basket cleared.";
                    default -> "Basket cleared.";
                };
            case "total_price":
                return switch (lang) {
                    case "uz" -> "\nUmumiy narx: %s";
                    case "ru" -> "\nОбщая сумма: %s";
                    case "en" -> "\nTotal price: %s";
                    default -> "\nTotal price: %s";
                };
            default:
                return "Unknown message";
        }
    }
}