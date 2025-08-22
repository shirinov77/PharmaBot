package org.example.pharmaproject.bot;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.*;
import org.example.pharmaproject.bot.handlers.*;
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


@Component
public class PharmacyBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    private UserService userService;

    @Autowired
    private StartHandler startHandler;

    @Autowired
    private MenuHandler menuHandler;

    @Autowired
    private SearchHandler searchHandler;

    @Autowired
    private BasketHandler basketHandler;

    @Autowired
    private OrderHandler orderHandler;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleMessage(Message message) {
        String chatId = message.getChatId().toString();
        String text = message.hasText() ? message.getText() : "";

        User user = userService.findByTelegramId(message.getFrom().getId())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setTelegramId(message.getFrom().getId());
                    newUser.setName(message.getFrom().getFirstName());
                    newUser.setLanguage("uz");
                    return userService.save(newUser);
                });

        Object response; // <-- BotApiMethod yoki SendPhoto bo‘lishi mumkin

        if ("AWAITING_ADDRESS".equals(user.getState())) {
            response = handleAddressInput(message, user, text);
        } else {
            switch (text) {
                case "/start" -> response = startHandler.handleStart(message, user);
                case "Menyu", "/menu" -> response = menuHandler.handleMenu(message, user);
                case "Savat", "/basket" -> response = basketHandler.handleBasket(message, user);
                case "Buyurtmalarim", "/orders" -> response = orderHandler.handleOrders(message, user);
                case "Tilni o'zgartirish", "/language" ->
                        response = startHandler.handleLanguageSelection(message, user);
                case "Manzilni o'zgartirish", "/address" -> {
                    user.setState("AWAITING_ADDRESS");
                    userService.save(user);
                    response = new SendMessage(chatId, getLocalizedMessage(user.getLanguage(), "enter_address"));
                }
                default -> {
                    if (text.startsWith("Qidir: ")) {
                        String query = text.substring(7).trim();
                        response = searchHandler.handleSearch(message, query, user);
                    } else {
                        response = new SendMessage(chatId, getLocalizedMessage(user.getLanguage(), "unknown_command"));
                    }
                }
            }
        }

        executeResponse(response);
    }

    private BotApiMethod<?> handleAddressInput(Message message, User user, String address) {
        String chatId = message.getChatId().toString();
        userService.updateUserDetails(user.getTelegramId(), null, null, address);
        user.setState(null);
        userService.save(user);

        SendMessage response = new SendMessage(chatId, getLocalizedMessage(user.getLanguage(), "address_updated"));
        response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        return response;
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String chatId = callbackQuery.getMessage().getChatId().toString();
        String data = callbackQuery.getData();
        int messageId = callbackQuery.getMessage().getMessageId();

        User user = userService.findByTelegramId(callbackQuery.getFrom().getId())
                .orElse(null);

        Object response;

        try {
            if (user == null) throw new IllegalStateException("Foydalanuvchi topilmadi");

            if (data.startsWith("category_")) {
                String categoryId = data.substring(9);
                response = menuHandler.handleCategorySelection(callbackQuery, categoryId);
            } else if (data.startsWith("product_")) {
                String productId = data.substring(8, data.lastIndexOf("_"));
                if (data.endsWith("_add")) {
                    response = basketHandler.handleAddToBasket(callbackQuery, productId);
                } else {
                    response = searchHandler.handleProductDetails(callbackQuery, productId); // <-- bu SendPhoto qaytaradi
                }
            } else if (data.startsWith("basket_")) {
                if (data.equals("basket_clear")) {
                    response = basketHandler.handleClearBasket(callbackQuery);
                } else if (data.equals("basket_checkout")) {
                    response = orderHandler.handleCheckout(callbackQuery);
                } else if (data.startsWith("basket_remove_")) {
                    String productId = data.substring(13);
                    response = basketHandler.handleRemoveFromBasket(callbackQuery, productId);
                } else {
                    response = defaultCallbackResponse(chatId, messageId, user);
                }
            } else if (data.startsWith("order_")) {
                String orderId = data.substring(6, data.lastIndexOf("_"));
                if (data.endsWith("_confirm")) {
                    response = orderHandler.handleConfirmOrder(callbackQuery, orderId);
                } else if (data.endsWith("_cancel")) {
                    response = orderHandler.handleCancelOrder(callbackQuery, orderId);
                } else {
                    response = defaultCallbackResponse(chatId, messageId, user);
                }
            } else if (data.startsWith("lang_")) {
                String lang = data.substring(5);
                response = startHandler.handleLanguageChange(callbackQuery, lang);
            } else {
                response = defaultCallbackResponse(chatId, messageId, user);
            }

        } catch (Exception e) {
            response = defaultCallbackResponse(chatId, messageId, user);
        }

        executeResponse(response);
    }

    private EditMessageText defaultCallbackResponse(String chatId, int messageId, User user) {
        String lang = user != null ? user.getLanguage() : "uz";
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(getLocalizedMessage(lang, "invalid_callback"));
        return editMessage;
    }

    /**
     * Har qanday javobni (SendMessage, SendPhoto, EditMessageText) yuboradi
     */
    private void executeResponse(Object response) {
        if (response == null) return;
        try {
            if (response instanceof BotApiMethod<?> botApiMethod) {
                execute(botApiMethod);
            } else if (response instanceof SendPhoto sendPhoto) {
                execute(sendPhoto);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String getLocalizedMessage(String lang, String key) {
        return switch (key) {
            case "enter_address" -> switch (lang) {
                case "uz" -> "Yangi manzilingizni kiriting:";
                case "ru" -> "Введите новый адрес:";
                case "en" -> "Enter your new address:";
                default -> "Enter your new address:";
            };
            case "address_updated" -> switch (lang) {
                case "uz" -> "✅ Manzil yangilandi!";
                case "ru" -> "✅ Адрес обновлён!";
                case "en" -> "✅ Address updated!";
                default -> "Address updated!";
            };
            case "unknown_command" -> switch (lang) {
                case "uz" -> "❓ Noto‘g‘ri buyruq. Iltimos, menyudan foydalaning.";
                case "ru" -> "❓ Неверная команда. Пожалуйста, используйте меню.";
                case "en" -> "❓ Invalid command. Please use the menu.";
                default -> "Invalid command.";
            };
            case "invalid_callback" -> switch (lang) {
                case "uz" -> "❌ Noto‘g‘ri callback ma’lumotlari.";
                case "ru" -> "❌ Неверные данные callback.";
                case "en" -> "❌ Invalid callback data.";
                default -> "Invalid callback data.";
            };
            case "unknown_action" -> switch (lang) {
                case "uz" -> "❓ Noma’lum amal. Iltimos, qaytadan urinib ko‘ring.";
                case "ru" -> "❓ Неизвестное действие. Попробуйте снова.";
                case "en" -> "❓ Unknown action. Please try again.";
                default -> "Unknown action.";
            };
            default -> "Unknown message";
        };
    }
}
