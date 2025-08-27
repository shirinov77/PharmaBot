package org.example.pharmaproject.bot.handlers;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.Order;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.BasketService;
import org.example.pharmaproject.services.OrderService;
import org.example.pharmaproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class OrderHandler {

    private final OrderService orderService;
    private final UserService userService;
    private final BasketService basketService;

    @Autowired
    public OrderHandler(OrderService orderService, UserService userService, BasketService basketService) {
        this.orderService = orderService;
        this.userService = userService;
        this.basketService = basketService;
    }

    /** Foydalanuvchining buyurtmalar ro‘yxatini ko‘rsatish */
    public BotApiMethod<?> handleOrders(Message message, User user) {
        String chatId = message.getChatId().toString();

        List<Order> orders = orderService.findOrdersByUser(user);

        String text = orders.isEmpty()
                ? BotUtils.getLocalizedMessage(user.getLanguage(), "n_orders")
                : getOrdersSummary(orders, user.getLanguage());

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.createOrdersInlineKeyboard(orders, user.getLanguage()));
        return response;
    }

    /** Buyurtma berish jarayonini boshlash */
    public BotApiMethod<?> handleCheckout(CallbackQuery query, User user) {
        String chatId = query.getMessage().getChatId().toString();
        Basket basket = basketService.getBasketByUser(user);

        if (basket.getProducts().isEmpty()) {
            return new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "empty_basket"));
        }

        if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
            user.setState("AWAITING_ADDRESS");
            userService.save(user);
            return new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "enter_address"));
        } else {
            return handleFinalizeOrder(query.getMessage(), user);
        }
    }

    /** Foydalanuvchi manzilini kiritgandan so‘ng buyurtmani yakunlash */
    public BotApiMethod<?> handleFinalizeOrder(Message message, User user) {
        String chatId = message.getChatId().toString();
        String address = message.getText().trim();

        if ("AWAITING_ADDRESS".equals(user.getState())) {
            userService.updateUserDetails(user.getTelegramId(), null, null, address);
            user.setState(null);
            userService.save(user);
        }

        try {
            Order order = orderService.createOrderFromBasket(user);
            String successMessage = String.format(
                    BotUtils.getLocalizedMessage(user.getLanguage(), "order_created"),
                    order.getId(),
                    user.getAddress()
            );

            SendMessage response = new SendMessage(chatId, successMessage);
            response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
            return response;

        } catch (IllegalStateException e) {
            return new SendMessage(chatId, "❌ " + e.getMessage());
        }
    }

    /** Buyurtma statusini o‘zgartirish */
    public BotApiMethod<?> updateStatus(CallbackQuery query, Long orderId, String status) {
        String chatId = query.getMessage().getChatId().toString();
        User user = userService.findByTelegramId(query.getFrom().getId()).orElseThrow();

        try {
            if ("confirm".equals(status)) {
                orderService.updateStatus(orderId, Order.Status.CONFIRMED);
                return new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "order_confirmed"));
            } else if ("cancel".equals(status)) {
                orderService.updateStatus(orderId, Order.Status.CANCELLED);
                return new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "order_cancelled"));
            }
        } catch (Exception e) {
            return new SendMessage(chatId, "❌ " + e.getMessage());
        }
        return new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "invalid_callback"));
    }

    /** Buyurtmalar haqida umumiy ma'lumot matnini yaratish */
    private String getOrdersSummary(List<Order> orders, String lang) {
        StringBuilder summary = new StringBuilder(BotUtils.getLocalizedMessage(lang, "orders_summary"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (Order order : orders) {
            String statusValue = order.getStatus() != null ? order.getStatus().toString().toLowerCase() : "unknown";

            summary.append("\n\n#").append(order.getId())
                    .append("\n💰 ").append(String.format("%,.0f so‘m", order.getTotalPrice()))
                    .append("\n🕒 ").append(order.getCreatedAt().format(formatter))
                    .append("\n📊 ").append(BotUtils.getLocalizedMessage(lang, "order_status_" + statusValue));
        }
        return summary.toString();
    }
}
