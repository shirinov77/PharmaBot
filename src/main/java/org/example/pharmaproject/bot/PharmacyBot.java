package org.example.pharmaproject.bot;

import org.example.pharmaproject.bot.handlers.*;
import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class PharmacyBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = Logger.getLogger(PharmacyBot.class.getName());

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

    @Autowired
    public PharmacyBot(UserService userService,
                       StartHandler startHandler,
                       MenuHandler menuHandler,
                       SearchHandler searchHandler,
                       BasketHandler basketHandler,
                       OrderHandler orderHandler) {
        this.userService = userService;
        this.startHandler = startHandler;
        this.menuHandler = menuHandler;
        this.searchHandler = searchHandler;
        this.basketHandler = basketHandler;
        this.orderHandler = orderHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Xato onUpdateReceived ichida: " + e.getMessage(), e);
            // executeResponse(new SendMessage(update.getMessage().getChatId().toString(), "❌ Kutilmagan xatolik yuz berdi."));
        }
    }

    private void handleMessage(Message message) {
        String chatId = message.getChatId().toString();
        String text = message.hasText() ? message.getText().trim() : "";

        User user = userService.findByTelegramId(message.getFrom().getId())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setTelegramId(message.getFrom().getId());
                    newUser.setName(message.getFrom().getFirstName());
                    newUser.setLanguage(null);
                    newUser.setState(null);
                    return userService.save(newUser);
                });

        BotApiMethod<?> response;
        try {
            if ("AWAITING_ADDRESS".equals(user.getState())) {
                response = orderHandler.handleFinalizeOrder(message, user);
            } else {
                String menuText = BotUtils.getLocalizedMessage(user.getLanguage(), "menu");
                String searchText = BotUtils.getLocalizedMessage(user.getLanguage(), "search");
                String basketText = BotUtils.getLocalizedMessage(user.getLanguage(), "basket");
                String ordersText = BotUtils.getLocalizedMessage(user.getLanguage(), "orders");
                String changeLangText = BotUtils.getLocalizedMessage(user.getLanguage(), "change_language");

                if (text.equalsIgnoreCase("/start")) {
                    response = startHandler.handleStart(message);
                } else if (text.equalsIgnoreCase(menuText)) {
                    response = menuHandler.handleMenu(message, user);
                } else if (text.equalsIgnoreCase(searchText)) {
                    response = searchHandler.handleSearchPrompt(message, user);
                } else if (text.equalsIgnoreCase(basketText)) {
                    response = basketHandler.handleBasket(message, user);
                } else if (text.equalsIgnoreCase(ordersText)) {
                    response = orderHandler.handleOrders(message, user);
                } else if (text.equalsIgnoreCase(changeLangText)) {
                    response = startHandler.handleLanguageSelection(message, user);
                } else if (text.startsWith("Qidir: ") || text.startsWith("🔎 Поиск: ") || text.startsWith("🔍 Search: ")) {
                    String query = text.substring(text.indexOf(":") + 1).trim();
                    response = searchHandler.handleSearch(message, query, user);
                } else {
                    response = new SendMessage(chatId,
                            BotUtils.getLocalizedMessage(user.getLanguage(), "unknown_command"));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "❌ Xabar ishlashda xato: " + e.getMessage(), e);
            response = new SendMessage(chatId, "❌ Kutilmagan xatolik yuz berdi. Iltimos, qaytadan urinib ko‘ring.");
        }
        executeResponse(response);
    }

    private void handleCallbackQuery(CallbackQuery query) {
        String chatId = query.getMessage().getChatId().toString();
        String data = query.getData();
        int messageId = query.getMessage().getMessageId();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new IllegalStateException("Foydalanuvchi topilmadi!"));

        BotApiMethod<?> response;
        try {
            if (data.startsWith("category_")) {
                String categoryId = data.substring("category_".length());
                response = menuHandler.handleCategorySelection(query, categoryId);
            } else if (data.startsWith("product_")) {
                String productId = data.substring("product_".length());
                response = menuHandler.handleProductDetails(query, productId);
            } else if (data.startsWith("addtobasket_")) {
                String productId = data.substring("addtobasket_".length());
                response = basketHandler.handleAddToBasket(query, productId);
            } else if (data.startsWith("basket_")) {
                String[] parts = data.split("_");
                String action = parts[1];
                switch (action) {
                    case "increase":
                        response = basketHandler.handleIncreaseProductCount(query, parts[2]);
                        break;
                    case "decrease":
                        response = basketHandler.handleDecreaseProductCount(query, parts[2]);
                        break;
                    case "remove":
                        response = basketHandler.handleRemoveFromBasket(query, parts[2]);
                        break;
                    case "clear":
                        response = basketHandler.handleClearBasket(query);
                        break;
                    case "checkout":
                        response = orderHandler.handleCheckout(query, user);
                        break;
                    default:
                        response = defaultCallbackResponse(chatId, messageId, user);
                }
            } else if (data.startsWith("order_")) {
                String orderId = data.substring("order_".length());
                String status = data.substring(data.lastIndexOf("_") + 1);
                response = orderHandler.updateStatus(query, Long.parseLong(orderId), status);
            } else if (data.startsWith("lang_")) {
                String lang = data.substring("lang_".length());
                response = startHandler.handleLanguageChange(query, lang);
            } else {
                response = defaultCallbackResponse(chatId, messageId, user);
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "❌ Callback ishlashda xato: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "❌ Telegram API xatosi: " + e.getMessage(), e);
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