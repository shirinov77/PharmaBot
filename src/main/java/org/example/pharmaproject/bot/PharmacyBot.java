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
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Xato onUpdateReceived ichida: " + e.getMessage(), e);
        }
    }

    private void handleMessage(Message message) {
        String chatId = message.getChatId().toString();
        String text = message.getText().trim();

        User user = userService.findByTelegramId(message.getFrom().getId())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setTelegramId(message.getFrom().getId());
                    newUser.setName(message.getFrom().getFirstName());
                    newUser.setLanguage("uz");
                    newUser.setState(null);
                    return userService.save(newUser);
                });

        BotApiMethod<?> response;

        try {
            if ("AWAITING_ADDRESS".equals(user.getState())) {
                response = orderHandler.handleFinalizeOrder(message, user);
            } else {
                switch (text) {
                    case "/start":
                        response = startHandler.handleStart(message);
                        break;
                    case "📁 Mahsulotlar":
                    case "📁 Products":
                    case "📁 Товары":
                        response = menuHandler.handleMenu(message, user);
                        break;
                    case "🔎 Qidirish":
                    case "🔎 Search":
                    case "🔎 Поиск":
                        response = searchHandler.handleSearchPrompt(message, user);
                        break;
                    case "🛒 Savat":
                    case "🛒 Basket":
                    case "🛒 Корзина":
                        response = basketHandler.handleBasket(message, user);
                        break;
                    case "📜 Buyurtmalarim":
                    case "📜 Orders":
                    case "📜 Заказы":
                        response = orderHandler.handleOrders(message, user);
                        break;
                    case "🌐 Smenit til":
                    case "🌐 Change Language":
                    case "🌐 Сменить язык":
                        response = startHandler.handleLanguageSelection(message, user);
                        break;
                    default:
                        if (text.startsWith("Qidir: ") || text.startsWith("🔎 Поиск: ") || text.startsWith("🔍 Search: ")) {
                            String query = text.substring(text.indexOf(":") + 1).trim();
                            response = searchHandler.handleSearch(message, query, user);
                        } else {
                            response = new SendMessage(chatId,
                                    BotUtils.getLocalizedMessage(user.getLanguage(), "unknown_command"));
                        }
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
                response = basketHandler.handleAddToBasket(query, data.substring(15));
            } else if (data.startsWith("basket_")) {
                String[] parts = data.split("_");
                if (parts.length < 2) {
                    response = defaultCallbackResponse(chatId, messageId, user);
                } else {
                    switch (parts[1]) {
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
                }
            } else if (data.startsWith("order_")) {
                String[] orderParts = data.substring(6).split("_");
                if(orderParts.length >= 2) {
                    String orderId = orderParts[0];
                    String status = orderParts[1];
                    response = orderHandler.updateStatus(query, Long.parseLong(orderId), status);
                } else {
                    response = defaultCallbackResponse(chatId, messageId, user);
                }
            } else if (data.startsWith("lang_")) {
                String lang = data.substring(5);
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
