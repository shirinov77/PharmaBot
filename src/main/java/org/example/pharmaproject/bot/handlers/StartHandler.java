package org.example.pharmaproject.bot.handlers;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class StartHandler {

    private final UserService userService;

    @Autowired
    public StartHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * /start komandasi
     */
    public BotApiMethod<?> handleStart(Message message, User user) {
        String chatId = message.getChatId().toString();
        Long telegramId = message.getFrom().getId();

        // DB dan userni olish yoki yangi yaratish
        user = userService.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setTelegramId(telegramId);
                    newUser.setName(message.getFrom().getFirstName() != null ? message.getFrom().getFirstName() : "Foydalanuvchi");
                    newUser.setLanguage(null); // startda til tanlash majburiy
                    return userService.save(newUser);
                });

        // Til tanlanmagan bo‘lsa → til tanlash menyusini chiqaramiz
        if (user.getLanguage() == null) {
            SendMessage response = new SendMessage(chatId,
                    "🌐 Iltimos, tilni tanlang:");
            response.setReplyMarkup(BotUtils.createLanguageInlineKeyboard());
            return response;
        }

        // Aks holda → asosiy menyu
        SendMessage response = new SendMessage(chatId,
                BotUtils.getLocalizedMessage(user.getLanguage(), "welcome_message"));
        response.setReplyMarkup(BotUtils.getMainKeyboard(user.getLanguage()));
        return response;
    }

    /**
     * Tilni o‘zgartirish (callback orqali)
     */
    public BotApiMethod<?> handleLanguageChange(CallbackQuery query, String lang) {
        String chatId = query.getMessage().getChatId().toString();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        user.setLanguage(lang);
        userService.save(user);

        // ReplyKeyboardMarkup bilan SendMessage ishlatamiz
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("✅ Til muvaffaqiyatli o‘zgartirildi!");
        sendMessage.setReplyMarkup(BotUtils.getMainKeyboard(lang)); // asosiy menyu
        return sendMessage;
    }


    /**
     * /language yoki "Tilni o‘zgartirish" tugmasi
     */
    public BotApiMethod<?> handleLanguageSelection(Message message, User user) {
        String chatId = message.getChatId().toString();
        SendMessage response = new SendMessage(chatId, "🌐 Iltimos, tilni tanlang:");
        response.setReplyMarkup(BotUtils.createLanguageInlineKeyboard());
        return response;
    }
}
