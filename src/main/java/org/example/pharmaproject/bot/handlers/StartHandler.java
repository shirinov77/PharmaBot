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
     * Foydalanuvchi /start komandasini yuborganda ishga tushadi.
     * Yangi foydalanuvchini aniqlaydi va til tanlash menyusini yuboradi.
     * Mavjud foydalanuvchiga esa to'g'ridan-to'g'ri asosiy menyuni ko'rsatadi.
     */
    public BotApiMethod<?> handleStart(Message message) {
        String chatId = message.getChatId().toString();
        Long telegramId = message.getFrom().getId();

        Optional<User> optionalUser = userService.findByTelegramId(telegramId);

        // Agar foydalanuvchi ma'lumotlar bazasida mavjud bo'lmasa, uni yangi foydalanuvchi sifatida saqlaymiz va til tanlash menyusini yuboramiz
        if (optionalUser.isEmpty()) {
            User newUser = new User();
            newUser.setTelegramId(telegramId);
            newUser.setName(message.getFrom().getFirstName() != null ? message.getFrom().getFirstName() : "Foydalanuvchi");
            newUser.setLanguage(null); // Til tanlanmagan
            userService.save(newUser);

            SendMessage response = new SendMessage(chatId,
                    BotUtils.getLocalizedMessage("uz", "select_language"));
            response.setReplyMarkup(BotUtils.createLanguageInlineKeyboard());
            return response;
        }

        // Aks holda → asosiy menyu
        User user = optionalUser.get();
        SendMessage response = new SendMessage(chatId,
                BotUtils.getLocalizedMessage(user.getLanguage(), "welcome_message"));
        response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        return response;
    }

    /**
     * Foydalanuvchi Tilni o'zgartirish tugmasini bosganda yoki /language komandasini yuborganda ishga tushadi.
     * Til tanlash uchun inline menyu yuboradi.
     */
    public BotApiMethod<?> handleLanguageSelection(Message message, User user) {
        String chatId = message.getChatId().toString();
        SendMessage response = new SendMessage(chatId, BotUtils.getLocalizedMessage(user.getLanguage(), "select_language"));
        response.setReplyMarkup(BotUtils.createLanguageInlineKeyboard());
        return response;
    }

    /**
     * Foydalanuvchi tilni inline menyudan tanlaganda ishga tushadi (callback orqali).
     * Foydalanuvchining tilini ma'lumotlar bazasida yangilaydi va asosiy menyuni yuboradi.
     */
    public BotApiMethod<?> handleLanguageChange(CallbackQuery query, String lang) {
        String chatId = query.getMessage().getChatId().toString();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        user.setLanguage(lang);
        userService.save(user);

        // Til muvaffaqiyatli o'zgartirilgach, asosiy menyuga o'tish
        String successMessage = BotUtils.getLocalizedMessage(lang, "language_changed");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(successMessage);
        sendMessage.setReplyMarkup(BotUtils.getMainKeyboard(lang));
        return sendMessage;
    }
}