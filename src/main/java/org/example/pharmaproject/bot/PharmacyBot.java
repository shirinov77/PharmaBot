package org.example.pharmaproject.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.pharmaproject.bot.handlers.*;
import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PharmacyBot extends TelegramLongPollingBot {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Value("${telegrambot.userName}")
    private String botUsername;

    @Value("${telegrambot.botToken}")
    private String botToken;

    private final UserService userService;
    private final StartHandler startHandler;
    private final MenuHandler menuHandler;
    private final SearchHandler searchHandler;
    private final BasketHandler basketHandler;
    private final OrderHandler orderHandler;

    private static final Map<String, String> REPLY_KEYBOARD_COMMANDS = Map.ofEntries(
            Map.entry(BotUtils.getLocalizedMessage("uz", "menu_button"), "PRODUCTS"),
            Map.entry(BotUtils.getLocalizedMessage("en", "menu_button"), "PRODUCTS"),
            Map.entry(BotUtils.getLocalizedMessage("ru", "menu_button"), "PRODUCTS"),
            Map.entry(BotUtils.getLocalizedMessage("uz", "basket_button"), "BASKET"),
            Map.entry(BotUtils.getLocalizedMessage("en", "basket_button"), "BASKET"),
            Map.entry(BotUtils.getLocalizedMessage("ru", "basket_button"), "BASKET"),
            Map.entry(BotUtils.getLocalizedMessage("uz", "orders_button"), "ORDERS"),
            Map.entry(BotUtils.getLocalizedMessage("en", "orders_button"), "ORDERS"),
            Map.entry(BotUtils.getLocalizedMessage("ru", "orders_button"), "ORDERS"),
            Map.entry(BotUtils.getLocalizedMessage("uz", "language_button"), "LANGUAGE"),
            Map.entry(BotUtils.getLocalizedMessage("en", "language_button"), "LANGUAGE"),
            Map.entry(BotUtils.getLocalizedMessage("ru", "language_button"), "LANGUAGE"),
            Map.entry(BotUtils.getLocalizedMessage("uz", "search_button"), "SEARCH"),
            Map.entry(BotUtils.getLocalizedMessage("en", "search_button"), "SEARCH"),
            Map.entry(BotUtils.getLocalizedMessage("ru", "search_button"), "SEARCH")
    );

    @Override
    public void onUpdateReceived(Update update) {
        executor.submit(() -> {
            try {
                if (update.hasMessage() && update.getMessage().hasText()) {
                    handleMessage(update.getMessage());
                } else if (update.hasCallbackQuery()) {
                    handleCallbackQuery(update.getCallbackQuery());
                }
            } catch (Exception e) {
                log.error("❌ onUpdateReceived xatosi: {}", e.getMessage(), e);
            }
        });
    }

    private void handleMessage(Message message) {
        String chatId = message.getChatId().toString();
        Long telegramId = message.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException("Foydalanuvchi topilmadi!"));
        String text = message.getText().trim();
        BotApiMethod<?> response;

        try {
            String state = user.getState();
            if (state != null) {
                if ("AWAITING_PHONE".equals(state) || "AWAITING_ADDRESS".equals(state)) {
                    response = orderHandler.handleUserInput(message, user);
                } else {
                    response = new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "unknown_command"));
                }
            } else if (text.startsWith("/")) {
                response = switch (text) {
                    case "/start" -> startHandler.handleStart(message);
                    case "/language" -> startHandler.handleLanguageSelection(message, user);
                    default -> new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "unknown_command"));
                };
            } else if (REPLY_KEYBOARD_COMMANDS.containsKey(text)) {
                String command = REPLY_KEYBOARD_COMMANDS.get(text);
                response = switch (command) {
                    case "PRODUCTS" -> menuHandler.handleMenu(message, user);
                    case "BASKET" -> basketHandler.handleBasket(message, user);
                    case "ORDERS" -> orderHandler.handleOrders(message, user);
                    case "LANGUAGE" -> startHandler.handleLanguageSelection(message, user);
                    case "SEARCH" -> searchHandler.handleSearchPrompt(message, user);
                    default -> new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "unknown_command"));
                };
            } else {
                response = searchHandler.handleSearch(message, text, user);
            }
        } catch (Exception e) {
            log.warn("❌ Xabar ishlov berishda xato: {}", e.getMessage(), e);
            response = new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "error_message"));
        }

        executeResponse(response);
    }

    private void handleCallbackQuery(CallbackQuery query) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();
        String data = query.getData();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new IllegalStateException("Foydalanuvchi topilmadi!"));

        BotApiMethod<?> response;
        try {
            if (data.startsWith("category_")) {
                response = menuHandler.handleCategorySelection(query, data.substring(9));
            } else if (data.startsWith("product_")) {
                response = menuHandler.handleProductDetails(query, data.substring(8));
            } else if (data.startsWith("add_to_basket_")) {
                response = basketHandler.handleAddToBasket(query, data.substring(14));
            } else if (data.startsWith("basket_")) {
                String[] parts = data.split("_");
                if (parts.length >= 3) {
                    response = switch (parts[1]) {
                        case "increase" -> basketHandler.handleIncreaseProductCount(query, parts[2]);
                        case "decrease" -> basketHandler.handleDecreaseProductCount(query, parts[2]);
                        case "remove" -> basketHandler.handleRemoveFromBasket(query, parts[2]);
                        default -> defaultCallbackResponse(chatId, messageId, user);
                    };
                } else if ("clear".equals(parts[1])) {
                    response = basketHandler.handleClearBasket(query);
                } else if ("checkout".equals(parts[1])) {
                    response = orderHandler.handleCheckout(query, user);
                } else {
                    response = defaultCallbackResponse(chatId, messageId, user);
                }
            } else if (data.startsWith("order_")) {
                String[] orderParts = data.substring(6).split("_");
                response = (orderParts.length >= 2)
                        ? orderHandler.updateStatus(query, Long.parseLong(orderParts[0]), orderParts[1])
                        : defaultCallbackResponse(chatId, messageId, user);
            } else if (data.startsWith("lang_")) {
                response = startHandler.handleLanguageChange(query, data);
            } else {
                response = defaultCallbackResponse(chatId, messageId, user);
            }
        } catch (Exception e) {
            log.warn("❌ Callback ishlov berishda xato: {}", e.getMessage(), e);
            response = defaultCallbackResponse(chatId, messageId, user);
        }

        executeResponse(response);
    }

    private EditMessageText defaultCallbackResponse(String chatId, int messageId, User user) {
        String lang = user != null ? user.getLanguage() : "uz";
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(BotUtils.getLocalizedMessage(lang, "invalid_callback"));
        return editMessage;
    }

    private void executeResponse(BotApiMethod<?> response) {
        if (response == null) return;
        try {
            execute(response);
        } catch (TelegramApiException e) {
            log.error("❌ Telegram API xatosi: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}