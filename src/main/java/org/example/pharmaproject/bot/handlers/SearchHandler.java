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
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * Foydalanuvchidan kelgan qidiruv so'rovini qayta ishlash
     * Takomillashtirilgan versiya: Nom, sinonim va teglarga asoslangan qidiruv
     */
    public BotApiMethod<?> handleSearch(Message message, String query, User user) {
        String chatId = message.getChatId().toString();

        List<Product> products = productService.searchByName(query);
        // Takomillashtirilgan ProductService'dagi qo'shimcha metodlar bilan qidiruvni kengaytiramiz
        // List<Product> productsByTags = productService.searchByTags(query);
        // List<Product> fuzzyProducts = productService.searchByFuzzyName(query);

        // Natijalarni birlashtiramiz va takrorlanishni olib tashlaymiz
        // List<Product> allResults = Stream.of(productsByName, productsByTags, fuzzyProducts)
        //                                   .flatMap(List::stream)
        //                                   .distinct()
        //                                   .collect(Collectors.toList());

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

    /**
     * Foydalanuvchiga qidiruv uchun prompt yuborish
     */
    public SendMessage handleSearchPrompt(Message message, User user) {
        String chatId = message.getChatId().toString();
        String promptText = BotUtils.getLocalizedMessage(user.getLanguage(), "enter_search_query");
        SendMessage response = new SendMessage(chatId, promptText);
        // Orqaga qaytish tugmasi
        response.setReplyMarkup(BotUtils.createBackToMenuKeyboard(user.getLanguage()));
        return response;
    }

    /**
     * Mahsulot tafsilotlarini ko‘rsatish (qidiruv natijasidan)
     * Bu metod MenuHandler'dagiga o‘xshash bo‘lgani uchun, u yerga birlashtirildi
     */
    // public BotApiMethod<?> handleProductDetails(CallbackQuery query, String productId) {
    //    return menuHandler.handleProductDetails(query, productId);
    // }
}