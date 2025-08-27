package org.example.pharmaproject.bot.handlers;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.ProductService;
import org.example.pharmaproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
public class SearchHandler {

    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public SearchHandler(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    /**
     * Foydalanuvchidan qidiruv so'rovini oladi va natijalarni qaytaradi
     */
    public BotApiMethod<?> handleSearch(Message message, String query, User user) {
        String chatId = message.getChatId().toString();

        // Mahsulotlarni nom bo‘yicha qidirish
        List<Product> products = productService.searchByName(query);

        if (products.isEmpty()) {
            String noResultsText = BotUtils.getLocalizedMessage(user.getLanguage(), "no_results");
            return new SendMessage(chatId, noResultsText);
        } else {
            StringBuilder textBuilder = new StringBuilder();
            textBuilder.append(BotUtils.getLocalizedMessage(user.getLanguage(), "search_results")).append("\n\n");

            for (Product product : products) {
                textBuilder.append("💊 ").append(product.getName()).append("\n")
                        .append("💵 ").append(String.format("%,.0f so‘m", product.getPrice())).append("\n\n");
            }

            SendMessage response = new SendMessage(chatId, textBuilder.toString());
            // Inline tugmalar yordamida har bir mahsulotni tanlash mumkin bo‘ladi
            response.setReplyMarkup(BotUtils.createProductsInlineKeyboard(products, user.getLanguage()));
            return response;
        }
    }

    /**
     * Foydalanuvchiga qidiruv promptini yuboradi
     */
    public SendMessage handleSearchPrompt(Message message, User user) {
        String chatId = message.getChatId().toString();
        String promptText = BotUtils.getLocalizedMessage(user.getLanguage(), "enter_search_query");
        SendMessage response = new SendMessage(chatId, promptText);
        // Orqaga qaytish tugmasi qo‘shiladi
        response.setReplyMarkup(BotUtils.createBackToMenuKeyboard(user.getLanguage()));
        return response;
    }

}
