package org.example.pharmaproject.bot.handlers;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;

@Component
public class StartHandler {

    private final UserService userService;

    @Autowired
    public StartHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * /start komanda ishlaganda
     * - Yangi foydalanuvchi → til tanlash
     * - Mavjud foydalanuvchi → asosiy menyu
     */
    public BotApiMethod<?> handleStart(Message message) {
        String chatId = message.getChatId().toString();
        Long telegramId = message.getFrom().getId();

        Optional<User> optionalUser = userService.findByTelegramId(telegramId);

        if (optionalUser.isEmpty()) {
            User newUser = new User();
            newUser.setTelegramId(telegramId);
            newUser.setName(message.getFrom().getFirstName() != null ? message.getFrom().getFirstName() : "Foydalanuvchi");
            newUser.setLanguage(null); // til tanlanmagan
            userService.save(newUser);

            SendMessage response = new SendMessage(chatId,
                    BotUtils.getLocalizedMessage("uz", "select_language"));
            response.setReplyMarkup(BotUtils.createLanguageInlineKeyboard());
            return response;
        }

        User user = optionalUser.get();
        SendMessage response = new SendMessage(chatId,
                BotUtils.getLocalizedMessage(user.getLanguage(), "welcome_message"));
        response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        return response;
    }

    /**
     * Til tanlash komandasini yuborish yoki /language komanda
     */
    public BotApiMethod<?> handleLanguageSelection(Message message, User user) {
        String chatId = message.getChatId().toString();
        SendMessage response = new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "select_language"));
        response.setReplyMarkup(BotUtils.createLanguageInlineKeyboard());
        return response;
    }

    /**
     * Foydalanuvchi tilni tanlaganda (callback)
     * Tilni yangilaydi va asosiy menyuni yuboradi
     */
    public BotApiMethod<?> handleLanguageChange(CallbackQuery query, String lang) {
        String chatId = query.getMessage().getChatId().toString();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        user.setLanguage(lang);
        userService.save(user);

        String successMessage = BotUtils.getLocalizedMessage(lang, "language_changed");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(successMessage);
        sendMessage.setReplyMarkup(BotUtils.getMainKeyboard(lang));
        return sendMessage;
    }
}
