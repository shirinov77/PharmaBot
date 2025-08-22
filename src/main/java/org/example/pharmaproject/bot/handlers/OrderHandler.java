package org.example.pharmaproject.bot.handlers;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.Order;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.OrderService;
import org.example.pharmaproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class OrderHandler {

    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public OrderHandler(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    /**
     * Buyurtmalar ro‘yxatini ko‘rsatish
     */
    public BotApiMethod<?> handleOrders(Message message, User user) {
        String chatId = message.getChatId().toString();

        List<Order> orders = orderService.findOrdersByUser(user);

        String text = orders.isEmpty()
                ? BotUtils.getLocalizedMessage(user.getLanguage(), "no_orders")
                : getOrdersSummary(orders, user.getLanguage());

        if (orders.isEmpty()) {
            SendMessage response = new SendMessage();
            response.setChatId(chatId);
            response.setText(text);
            response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
            return response;
        } else {
            SendMessage response = new SendMessage();
            response.setChatId(chatId);
            response.setText(text);
            response.setReplyMarkup(BotUtils.createOrdersInlineKeyboard(orders, user.getLanguage()));
            return response;
        }
    }



    /**
     * Checkout jarayonini boshlash
     */
    public BotApiMethod<?> handleCheckout(CallbackQuery query) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        Order order = orderService.createOrderFromBasket(user);

        String text = BotUtils.getLocalizedMessage(user.getLanguage(), "order_created") + " #" + order.getId();

        EditMessageText response = new EditMessageText();
        response.setChatId(chatId);
        response.setMessageId(messageId);
        response.setText(text);
        response.setReplyMarkup(BotUtils.createOrderActionsInline(order.getId(), user.getLanguage()));

        return response;
    }

    /**
     * Buyurtmani tasdiqlash
     */
    public List<BotApiMethod<?>> handleConfirmOrder(CallbackQuery query, String orderId) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        orderService.updateStatus(Long.parseLong(orderId), "CONFIRMED");

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(BotUtils.getLocalizedMessage(user.getLanguage(), "order_confirmed"));

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(BotUtils.getLocalizedMessage(user.getLanguage(), "back_to_menu"));
        sendMessage.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));

        return List.of(editMessage, sendMessage);
    }



    /**
     * Buyurtmani bekor qilish
     */
    public BotApiMethod<?> handleCancelOrder(CallbackQuery query, String orderId) {
        String chatId = query.getMessage().getChatId().toString();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        orderService.updateStatus(Long.parseLong(orderId), "CANCELLED");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(BotUtils.getLocalizedMessage(user.getLanguage(), "order_cancelled"));
        sendMessage.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));

        return sendMessage;
    }


    /**
     * Buyurtmalar haqida umumiy ma'lumot matnini yaratish
     */
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
