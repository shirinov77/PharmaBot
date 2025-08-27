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

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class PharmacyBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = Logger.getLogger(PharmacyBot.class.getName());
    private final ExecutorService executor = Executors.newFixedThreadPool(10); // bir vaqtda 10 ta oqim

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

    // tezkor tekshiruv uchun tayyor setlar
    private static final Set<String> PRODUCT_COMMANDS = Set.of("📁 Mahsulotlar", "📁 Products", "📁 Товары");
    private static final Set<String> SEARCH_COMMANDS = Set.of("🔎 Qidirish", "🔎 Search", "🔎 Поиск");
    private static final Set<String> BASKET_COMMANDS = Set.of("🛒 Savat", "🛒 Basket", "🛒 Корзина");
    private static final Set<String> ORDERS_COMMANDS = Set.of("📜 Buyurtmalarim", "📜 Orders", "📜 Заказы");
    private static final Set<String> LANGUAGE_COMMANDS = Set.of("🌐 Tilni o‘zgartirish", "🌐 Change Language", "🌐 Сменить язык");

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
        executor.submit(() -> { // har bir update alohida oqimda
            try {
                if (update.hasMessage() && update.getMessage().hasText()) {
                    handleMessage(update.getMessage());
                } else if (update.hasCallbackQuery()) {
                    handleCallbackQuery(update.getCallbackQuery());
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "❌ Xato onUpdateReceived ichida: " + e.getMessage(), e);
            }
        });
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
                    return userService.save(newUser);
                });

        BotApiMethod<?> response;
        try {
            if ("AWAITING_ADDRESS".equals(user.getState())) {
                response = orderHandler.handleFinalizeOrder(message, user);
            } else if ("/start".equals(text)) {
                response = startHandler.handleStart(message);
            } else if (PRODUCT_COMMANDS.contains(text)) {
                response = menuHandler.handleMenu(message, user);
            } else if (SEARCH_COMMANDS.contains(text)) {
                response = searchHandler.handleSearchPrompt(message, user);
            } else if (BASKET_COMMANDS.contains(text)) {
                response = basketHandler.handleBasket(message, user);
            } else if (ORDERS_COMMANDS.contains(text)) {
                response = orderHandler.handleOrders(message, user);
            } else if (LANGUAGE_COMMANDS.contains(text)) {
                response = startHandler.handleLanguageSelection(message, user);
            } else if (text.startsWith("Qidir: ") || text.startsWith("🔎 Поиск: ") || text.startsWith("🔍 Search: ")) {
                String query = text.substring(text.indexOf(":") + 1).trim();
                response = searchHandler.handleSearch(message, query, user);
            } else {
                response = new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "unknown_command"));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "❌ Xabar ishlashda xato: " + e.getMessage(), e);
            response = new SendMessage(chatId, "❌ Kutilmagan xatolik yuz berdi. Qaytadan urinib ko‘ring.");
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
                response = startHandler.handleLanguageChange(query, data.substring(5));
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
