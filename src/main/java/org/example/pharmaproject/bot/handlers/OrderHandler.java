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

import java.util.List;

@Component
public class OrderHandler {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    public BotApiMethod<?> handleOrders(Message message, User user) {
        String chatId = message.getChatId().toString();

        List<Order> orders = orderService.findOrdersByUser(user);
        String text = orders.isEmpty()
                ? getLocalizedMessage(user.getLanguage(), "no_orders")
                : getOrdersSummary(orders, user.getLanguage());

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.createOrdersInlineKeyboard(orders, user.getLanguage()));
        response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        return response;
    }

    public BotApiMethod<?> handleCheckout(CallbackQuery query) {
        String chatId = query.getMessage().getChatId().toString();
        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        Order order = orderService.createOrderFromBasket(user);
        String text = getLocalizedMessage(user.getLanguage(), "order_created") + order.getId();

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.createOrderActionsInline(order.getId(), user.getLanguage()));
        return response;
    }

    public BotApiMethod<?> handleConfirmOrder(CallbackQuery query, String orderId) {
        String chatId = query.getMessage().getChatId().toString();
        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        orderService.updateStatus(Long.parseLong(orderId), "CONFIRMED");

        String text = getLocalizedMessage(user.getLanguage(), "order_confirmed");

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        return response;
    }

    public BotApiMethod<?> handleCancelOrder(CallbackQuery query, String orderId) {
        String chatId = query.getMessage().getChatId().toString();
        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        orderService.updateStatus(Long.parseLong(orderId), "CANCELLED");

        String text = getLocalizedMessage(user.getLanguage(), "order_cancelled");

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        return response;
    }

    private String getOrdersSummary(List<Order> orders, String lang) {
        StringBuilder summary = new StringBuilder(getLocalizedMessage(lang, "orders_summary"));
        for (Order order : orders) {
            summary.append(String.format("\n- Buyurtma #%d: %s (%s)", order.getId(), order.getTotalPrice(), order.getStatus()));
        }
        return summary.toString();
    }

    private String getLocalizedMessage(String lang, String key) {
        switch (key) {
            case "orders_summary":
                return switch (lang) {
                    case "uz" -> "📦 Sizning buyurtmalaringiz:";
                    case "ru" -> "📦 Ваши заказы:";
                    case "en" -> "📦 Your orders:";
                    default -> "Your orders:";
                };
            case "no_orders":
                return switch (lang) {
                    case "uz" -> "❌ Hozircha buyurtmalar yo‘q.";
                    case "ru" -> "❌ У вас пока нет заказов.";
                    case "en" -> "❌ No orders yet.";
                    default -> "No orders yet.";
                };
            case "order_created":
                return switch (lang) {
                    case "uz" -> "✅ Buyurtma yaratildi: #";
                    case "ru" -> "✅ Заказ создан: #";
                    case "en" -> "✅ Order created: #";
                    default -> "Order created: #";
                };
            case "order_confirmed":
                return switch (lang) {
                    case "uz" -> "✅ Buyurtma tasdiqlandi.";
                    case "ru" -> "✅ Заказ подтверждён.";
                    case "en" -> "✅ Order confirmed.";
                    default -> "Order confirmed.";
                };
            case "order_cancelled":
                return switch (lang) {
                    case "uz" -> "❌ Buyurtma bekor qilindi.";
                    case "ru" -> "❌ Заказ отменён.";
                    case "en" -> "❌ Order cancelled.";
                    default -> "Order cancelled.";
                };
            default:
                return "Unknown message";
        }
    }
}