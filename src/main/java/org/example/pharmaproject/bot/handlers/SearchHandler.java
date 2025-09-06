package org.example.pharmaproject.bot.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.ProductService;
import org.example.pharmaproject.services.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchHandler {

    private final ProductService productService;
    private final UserService userService;

    public BotApiMethod<?> handleSearch(Message message, String query, User user) {
        String chatId = message.getChatId().toString();

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
            response.setReplyMarkup(BotUtils.createProductsInlineKeyboard(products, user.getLanguage()));
            return response;
        }
    }

    public SendMessage handleSearchPrompt(Message message, User user) {
        String chatId = message.getChatId().toString();
        String promptText = BotUtils.getLocalizedMessage(user.getLanguage(), "enter_search_query");
        SendMessage response = new SendMessage(chatId, promptText);
        response.setReplyMarkup(BotUtils.createBackToMenuKeyboard(user.getLanguage()));
        return response;
    }
}