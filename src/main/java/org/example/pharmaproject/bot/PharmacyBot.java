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

import java.util.Optional;
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
        }
    }

    private void handleMessage(Message message) {
        String chatId = message.getChatId().toString();
        String text = message.hasText() ? message.getText().trim() : "";

        User user = userService.findByTelegramId(message.getFrom().getId())
                .orElseGet(() -> createNewUser(message));

        Object response;

        try {
            if ("AWAITING_ADDRESS".equals(user.getState())) {
                response = handleAddressInput(message, user, text);
            } else {
                // Tugmalar matnlarini tilga qarab olish
                String menuText = BotUtils.getLocalizedMessage(user.getLanguage(), "menu");
                String searchText = BotUtils.getLocalizedMessage(user.getLanguage(), "search");
                String basketText = BotUtils.getLocalizedMessage(user.getLanguage(), "basket");
                String ordersText = BotUtils.getLocalizedMessage(user.getLanguage(), "orders");
                String changeLangText = BotUtils.getLocalizedMessage(user.getLanguage(), "change_language");
                String changeAddressText = BotUtils.getLocalizedMessage(user.getLanguage(), "change_address");

                if (text.equalsIgnoreCase(menuText)) {
                    response = menuHandler.handleMenu(message, user);
                } else if (text.equalsIgnoreCase(searchText)) {
                    response = searchHandler.handleSearchPrompt(message, user);
                } else if (text.equalsIgnoreCase(basketText)) {
                    response = basketHandler.handleBasket(message, user);
                } else if (text.equalsIgnoreCase(ordersText)) {
                    response = orderHandler.handleOrders(message, user);
                } else if (text.equalsIgnoreCase(changeLangText)) {
                    response = startHandler.handleLanguageSelection(message, user);
                } else if (text.equalsIgnoreCase(changeAddressText)) {
                    user.setState("AWAITING_ADDRESS");
                    userService.save(user);
                    response = new SendMessage(chatId,
                            BotUtils.getLocalizedMessage(user.getLanguage(), "enter_address"));
                } else if (text.startsWith("Qidir: ") || text.startsWith("🔎 Поиск: ") || text.startsWith("🔍 Search: ")) {
                    String query = text.substring(text.indexOf(":") + 1).trim();
                    response = searchHandler.handleSearch(message, query, user);
                } else if (text.equals("/start")) {
                    response = startHandler.handleStart(message, user);
                } else {
                    response = new SendMessage(chatId,
                            BotUtils.getLocalizedMessage(user.getLanguage(), "unknown_command"));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "❌ Message handle qilishda xato: " + e.getMessage(), e);
            response = new SendMessage(chatId, "❌ Xatolik yuz berdi. Iltimos, qaytadan urinib ko‘ring.");
        }

        executeResponse(response);
    }

    private User createNewUser(Message message) {
        User newUser = new User();
        newUser.setTelegramId(message.getFrom().getId());
        newUser.setName(Optional.ofNullable(message.getFrom().getFirstName()).orElse("Foydalanuvchi"));
        newUser.setLanguage("uz");
        newUser.setState(null);
        LOGGER.info("✅ Yangi foydalanuvchi qo‘shildi: " + newUser.getName());
        return userService.save(newUser);
    }

    private BotApiMethod<?> handleAddressInput(Message message, User user, String address) {
        String chatId = message.getChatId().toString();

        userService.updateUserDetails(user.getTelegramId(), null, null, address);
        user.setState(null);
        userService.save(user);

        SendMessage response = new SendMessage(chatId,
                BotUtils.getLocalizedMessage(user.getLanguage(), "address_updated"));
        response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        return response;
    }

    private void handleCallbackQuery(CallbackQuery query) {
        String chatId = query.getMessage().getChatId().toString();
        String data = query.getData();
        int messageId = query.getMessage().getMessageId();

        User user = userService.findByTelegramId(query.getFrom().getId()).orElse(null);
        Object response;

        try {
            if (user == null) throw new IllegalStateException("Foydalanuvchi topilmadi!");

            if (data.startsWith("category_")) {
                String categoryId = data.substring(9);
                response = menuHandler.handleCategorySelection(query, categoryId);
            } else if (data.startsWith("product_")) {
                String productId = data.substring(8, data.lastIndexOf("_"));
                response = data.endsWith("_add")
                        ? basketHandler.handleAddToBasket(query, productId)
                        : searchHandler.handleProductDetails(query, productId);
            } else if (data.startsWith("basket_")) {
                if (data.equals("basket_clear")) {
                    response = basketHandler.handleClearBasket(query);
                } else if (data.equals("basket_checkout")) {
                    response = orderHandler.handleCheckout(query);
                } else if (data.startsWith("basket_remove_")) {
                    String productId = data.substring(13);
                    response = basketHandler.handleRemoveFromBasket(query, productId);
                } else {
                    response = defaultCallbackResponse(chatId, messageId, user);
                }
            } else if (data.startsWith("order_")) {
                String orderId = data.substring(6, data.lastIndexOf("_"));
                if (data.endsWith("_confirm")) {
                    response = orderHandler.handleConfirmOrder(query, orderId);
                } else if (data.endsWith("_cancel")) {
                    response = orderHandler.handleCancelOrder(query, orderId);
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

    private void executeResponse(Object response) {
        if (response == null) return;
        try {
            if (response instanceof SendMessage sendMessage) {
                execute(sendMessage);
            } else if (response instanceof SendPhoto sendPhoto) {
                execute(sendPhoto);
            } else if (response instanceof EditMessageText editMessageText) {
                execute(editMessageText);
            } else if (response instanceof BotApiMethod<?> botApiMethod) {
                execute(botApiMethod);
            }
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
