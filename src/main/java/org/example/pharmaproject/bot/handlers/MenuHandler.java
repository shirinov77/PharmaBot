package org.example.pharmaproject.bot.handlers;

import org.example.pharmaproject.bot.utils.BotUtils;
import org.example.pharmaproject.entities.Category;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.CategoryService;
import org.example.pharmaproject.services.ProductService;
import org.example.pharmaproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.List;
import java.util.logging.Logger;

@Component
public class MenuHandler {
    private static final Logger LOGGER = Logger.getLogger(MenuHandler.class.getName());

    private final CategoryService categoryService;
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public MenuHandler(CategoryService categoryService, UserService userService, ProductService productService) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.productService = productService;
    }

    /**
     * Foydalanuvchiga barcha kategoriyalarni inline tugmalar bilan ko'rsatadi
     */
    public BotApiMethod<?> handleMenu(Message message, User user) {
        String chatId = message.getChatId().toString();
        List<Category> categories = categoryService.findAll();
        String text = BotUtils.getLocalizedMessage(user.getLanguage(), "menu_message");

        SendMessage response = new SendMessage(chatId, text);
        response.setReplyMarkup(BotUtils.createCategoryInlineKeyboard(categories, user.getLanguage()));
        return response;
    }

    /**
     * Foydalanuvchi kategoriya tugmasini bosganda, shu kategoriyadagi mahsulotlarni ko'rsatadi.
     * Endi bu metod yangi xabar yuborish o'rniga, mavjud xabarni tahrirlaydi (edit)
     */
    public BotApiMethod<?> handleCategorySelection(CallbackQuery query, String categoryId) {
        String chatId = query.getMessage().getChatId().toString();
        int messageId = query.getMessage().getMessageId();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        Category category = categoryService.findById(Long.parseLong(categoryId))
                .orElseThrow(() -> new RuntimeException("Kategoriya topilmadi"));

        List<Product> products = category.getProducts();

        // StringBuilder yordamida matnni samaraliroq yaratamiz
        StringBuilder textBuilder = new StringBuilder(BotUtils.getLocalizedMessage(user.getLanguage(), "category_selected") + category.getName());

        if (products.isEmpty()) {
            textBuilder.append("\n\n").append(BotUtils.getLocalizedMessage(user.getLanguage(), "no_products"));
            return EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(textBuilder.toString())
                    .replyMarkup(BotUtils.createBackToMenuKeyboard(user.getLanguage())) // Orqaga tugmasini qo'shish
                    .build();
        } else {
            textBuilder.append("\n\n").append(BotUtils.getLocalizedMessage(user.getLanguage(), "products_list"));
            for (Product product : products) {
                textBuilder.append(String.format("\n\n💊 %s\n💵 %s so‘m", product.getName(), String.format("%,.0f", product.getPrice())));
            }

            return EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(textBuilder.toString())
                    .replyMarkup(BotUtils.createProductsInlineKeyboard(products, user.getLanguage()))
                    .build();
        }
    }

    /**
     * Foydalanuvchi mahsulot tugmasini bosganda, uning to‘liq tafsilotlarini rasm bilan birga yuborish
     */
    public BotApiMethod<?> handleProductDetails(CallbackQuery query, String productId) {
        String chatId = query.getMessage().getChatId().toString();

        User user = userService.findByTelegramId(query.getFrom().getId())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        Product product = productService.findById(Long.parseLong(productId))
                .orElseThrow(() -> new RuntimeException("Mahsulot topilmadi"));

        // Caption (rasm ostidagi matn) yaratish
        String caption = String.format(
                BotUtils.getLocalizedMessage(user.getLanguage(), "product_details"),
                product.getName(),
                String.format("%,.0f so‘m", product.getPrice()),
                product.getQuantity()
        ) + (product.getDescription() != null ? "\n\n" + product.getDescription() : ""); // Tavsifni dinamik qo'shish

        // Mahsulot rasm URL'i bo'sh bo'lsa, default rasm yuboriladi
        InputFile photoFile;
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            photoFile = new InputFile(product.getImageUrl());
        } else {
            photoFile = new InputFile(getClass().getResourceAsStream("/static/no-image.png"), "no-image.png");
        }

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(photoFile);
        photo.setCaption(caption);
        photo.setParseMode("HTML"); // Matn formatini qo'shish uchun
        photo.setReplyMarkup(BotUtils.createProductDetailsInline(productId, user.getLanguage()));
        return photo;
    }

}
