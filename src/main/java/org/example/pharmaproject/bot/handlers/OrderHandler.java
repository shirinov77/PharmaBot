package org.example.pharmaproject.bot.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.Order;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.BasketService;
import org.example.pharmaproject.services.OrderService;
import org.example.pharmaproject.services.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderHandler {

    private final OrderService orderService;
    private final UserService userService;
    private final BasketService basketService;

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

    public BotApiMethod<?> handleCheckout(CallbackQuery query, User user) {
        String chatId = query.getMessage().getChatId().toString();
        Basket basket = basketService.getBasketByUser(user);

        if (basket.getProducts().isEmpty()) {
            return new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "empty_basket"));
        }

        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            user.setState("AWAITING_PHONE");
            userService.save(user);
            SendMessage response = new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "enter_phone"));
            response.setReplyMarkup(BotUtils.createPhoneRequestKeyboard(user.getLanguage()));
            return response;
        }

        if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
            user.setState("AWAITING_ADDRESS");
            userService.save(user);
            SendMessage response = new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "enter_address"));
            response.setReplyMarkup(BotUtils.createLocationRequestKeyboard(user.getLanguage()));
            return response;
        }

        return finalizeOrder(chatId, user);
    }

    public BotApiMethod<?> handleUserInput(Message message, User user) {
        String chatId = message.getChatId().toString();
        String state = user.getState();

        if ("AWAITING_PHONE".equals(state) && message.hasContact()) {
            String phone = message.getContact().getPhoneNumber();
            userService.updateUserDetails(user.getTelegramId(), null, phone, null);
            user.setState(null);
            userService.save(user);

            if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
                user.setState("AWAITING_ADDRESS");
                userService.save(user);
                SendMessage response = new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "enter_address"));
                response.setReplyMarkup(BotUtils.createLocationRequestKeyboard(user.getLanguage()));
                return response;
            }

            return finalizeOrder(chatId, user);
        } else if ("AWAITING_ADDRESS".equals(state) && message.hasLocation()) {
            String address = message.getLocation().getLatitude() + ", " + message.getLocation().getLongitude();
            userService.updateUserDetails(user.getTelegramId(), null, null, address);
            user.setState(null);
            userService.save(user);
            return finalizeOrder(chatId, user);
        }

        return new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "invalid_input"));
    }

    private BotApiMethod<?> finalizeOrder(String chatId, User user) {
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