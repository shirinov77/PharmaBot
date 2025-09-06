package org.example.pharmaproject.bot.utils;

import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.Category;
import org.example.pharmaproject.entities.Order;
import org.example.pharmaproject.entities.Product;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BotUtils {

    private static final Map<String, Map<String, String>> messages = new HashMap<>();

    static {
        initializeMessages();
    }

    public static InlineKeyboardMarkup createLanguageInlineKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(
                createButton("🇺🇿 O‘zbekcha", "lang_uz"),
                createButton("🇷🇺 Русский", "lang_ru"),
                createButton("🇬🇧 English", "lang_en")
        ));
        return new InlineKeyboardMarkup(rows);
    }

    public static InlineKeyboardMarkup createCategoryInlineKeyboard(List<Category> categories, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Category category : categories) {
            rows.add(List.of(createButton(category.getName(), "category_" + category.getId())));
        }
        rows.add(List.of(createButton(getLocalizedMessage(lang, "back_to_menu"), "BACK_TO_MENU")));
        return new InlineKeyboardMarkup(rows);
    }

    public static InlineKeyboardMarkup createProductsInlineKeyboard(List<Product> products, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Product product : products) {
            rows.add(List.of(createButton(product.getName(), "product_" + product.getId())));
        }
        rows.add(List.of(createButton(getLocalizedMessage(lang, "back_to_menu"), "BACK_TO_MENU")));
        return new InlineKeyboardMarkup(rows);
    }

    public static InlineKeyboardMarkup createProductDetailsInline(String productId, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(
                createButton(getLocalizedMessage(lang, "add_to_basket"), "add_to_basket_" + productId)
        ));
        rows.add(List.of(
                createButton(getLocalizedMessage(lang, "back_to_menu"), "BACK_TO_MENU")
        ));
        return new InlineKeyboardMarkup(rows);
    }

    public static InlineKeyboardMarkup createBasketManagementKeyboard(Basket basket, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        if (basket.getProducts() != null && !basket.getProducts().isEmpty()) {
            Map<Product, Long> productCounts = basket.getProducts().stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            for (Map.Entry<Product, Long> entry : productCounts.entrySet()) {
                Product product = entry.getKey();
                rows.add(List.of(
                        createButton("-", "basket_decrease_" + product.getId()),
                        createButton(entry.getValue().toString(), "IGNORE"),
                        createButton("+", "basket_increase_" + product.getId()),
                        createButton("❌", "basket_remove_" + product.getId())
                ));
            }
            rows.add(List.of(
                    createButton(getLocalizedMessage(lang, "clear_basket"), "basket_clear"),
                    createButton(getLocalizedMessage(lang, "checkout"), "basket_checkout")
            ));
        }
        rows.add(List.of(createButton(getLocalizedMessage(lang, "back_to_menu"), "BACK_TO_MENU")));
        return new InlineKeyboardMarkup(rows);
    }

    public static InlineKeyboardMarkup createOrdersInlineKeyboard(List<Order> orders, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() == Order.Status.PENDING) {
                rows.add(List.of(
                        createButton("#" + order.getId() + " ✅", "order_" + order.getId() + "_confirm"),
                        createButton("#" + order.getId() + " ❌", "order_" + order.getId() + "_cancel")
                ));
            }
        }
        rows.add(List.of(createButton(getLocalizedMessage(lang, "back_to_menu"), "BACK_TO_MENU")));
        return new InlineKeyboardMarkup(rows);
    }

    public static InlineKeyboardMarkup createBackToMenuKeyboard(String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(createButton(getLocalizedMessage(lang, "back_to_menu"), "BACK_TO_MENU")));
        return new InlineKeyboardMarkup(rows);
    }

    public static ReplyKeyboardMarkup getMainKeyboard(String lang) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(getLocalizedMessage(lang, "menu_button"));
        row1.add(getLocalizedMessage(lang, "basket_button"));
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(getLocalizedMessage(lang, "orders_button"));
        row2.add(getLocalizedMessage(lang, "search_button"));
        keyboard.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(getLocalizedMessage(lang, "language_button"));
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public static ReplyKeyboardMarkup createPhoneRequestKeyboard(String lang) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(getLocalizedMessage(lang, "share_phone"));
        button.setRequestContact(true);
        row.add(button);
        keyboard.add(row);

        KeyboardRow cancelRow = new KeyboardRow();
        cancelRow.add(getLocalizedMessage(lang, "cancel_button"));
        keyboard.add(cancelRow);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public static ReplyKeyboardMarkup createLocationRequestKeyboard(String lang) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(getLocalizedMessage(lang, "share_location"));
        button.setRequestLocation(true);
        row.add(button);
        keyboard.add(row);

        KeyboardRow cancelRow = new KeyboardRow();
        cancelRow.add(getLocalizedMessage(lang, "cancel_button"));
        keyboard.add(cancelRow);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public static String getLocalizedMessage(String lang, String key) {
        return messages.getOrDefault(lang.toLowerCase(), messages.get("uz"))
                .getOrDefault(key, key);
    }

    private static void initializeMessages() {
        // Uzbek
        Map<String, String> uz = new HashMap<>();
        uz.put("welcome_message", "Salom! Botimizga xush kelibsiz. 😊");
        uz.put("language_changed", "✅ Til muvaffaqiyatli o‘zgartirildi!");
        uz.put("select_language", "🌐 Iltimos, tilni tanlang:");
        uz.put("language_button", "🌐 Tilni o‘zgartirish");
        uz.put("menu_button", "📁 Mahsulotlar");
        uz.put("basket_button", "🛒 Savat");
        uz.put("orders_button", "📜 Mening buyurtmalarim");
        uz.put("search_button", "🔎 Qidiruv");
        uz.put("back_to_menu", "⬅️ Menyuga qaytish");
        uz.put("cancel_button", "❌ Bekor qilish");
        uz.put("add_to_basket", "🛒 Savatga qo‘shish");
        uz.put("clear_basket", "Savatni tozalash");
        uz.put("checkout", "Buyurtma berish");
        uz.put("unknown_command", "Kechirasiz, bu buyruq tan olinmadi.");
        uz.put("error_message", "❌ Kutilmagan xato yuz berdi. Iltimos, qayta urinib ko‘ring.");
        uz.put("n_orders", "Sizda buyurtmalar yo‘q.");
        uz.put("menu_message", "Kategoriyalar ro‘yxati:");
        uz.put("empty_basket", "Sizning savatingiz bo‘sh.");
        uz.put("enter_search_query", "Iltimos, qidiruv so‘rovini kiriting:");
        uz.put("no_results", "Natijalar topilmadi.");
        uz.put("search_results", "Qidiruv natijalari:");
        uz.put("order_created", "Buyurtma #%d muvaffaqiyatli yaratildi!\nManzil: %s");
        uz.put("order_confirmed", "Buyurtma tasdiqlandi.");
        uz.put("order_cancelled", "Buyurtma bekor qilindi.");
        uz.put("invalid_callback", "Noto‘g‘ri harakat.");
        uz.put("orders_summary", "Sizning buyurtmalaringiz:");
        uz.put("order_status_pending", "Kutilmoqda");
        uz.put("order_status_confirmed", "Tasdiqlangan");
        uz.put("order_status_cancelled", "Bekor qilingan");
        uz.put("product_added_to_basket", "Mahsulot savatga qo‘shildi.");
        uz.put("basket_cleared", "Savat tozalandi.");
        uz.put("enter_phone", "Iltimos, telefon raqamingizni baham ko'ring:");
        uz.put("enter_address", "Iltimos, manzilingizni baham ko'ring:");
        uz.put("share_phone", "📞 Telefonni baham ko'rish");
        uz.put("share_location", "📍 Manzilni baham ko'rish");
        uz.put("invalid_input", "Noto'g'ri kiritish. Iltimos, qayta urinib ko'ring.");
        messages.put("uz", uz);

        // Russian
        Map<String, String> ru = new HashMap<>();
        ru.put("welcome_message", "Привет! Добро пожаловать в наш бот. 😊");
        ru.put("language_changed", "✅ Язык успешно изменен!");
        ru.put("select_language", "🌐 Пожалуйста, выберите язык:");
        ru.put("language_button", "🌐 Изменить язык");
        ru.put("menu_button", "📁 Продукты");
        ru.put("basket_button", "🛒 Корзина");
        ru.put("orders_button", "📜 Мои заказы");
        ru.put("search_button", "🔎 Поиск");
        ru.put("back_to_menu", "⬅️ Вернуться в меню");
        ru.put("cancel_button", "❌ Отменить");
        ru.put("add_to_basket", "🛒 Добавить в корзину");
        ru.put("clear_basket", "Очистить корзину");
        ru.put("checkout", "Оформить заказ");
        ru.put("unknown_command", "Извините, эта команда непонятна.");
        ru.put("error_message", "❌ Произошла непредвиденная ошибка. Попробуйте снова.");
        ru.put("n_orders", "У вас нет заказов.");
        ru.put("menu_message", "Список категорий:");
        ru.put("empty_basket", "Корзина пуста.");
        ru.put("enter_search_query", "Пожалуйста, введите запрос для поиска:");
        ru.put("no_results", "Результатов не найдено.");
        ru.put("search_results", "Результаты поиска:");
        ru.put("order_created", "Заказ #%d успешно создан!\nАдрес: %s");
        ru.put("order_confirmed", "Заказ подтверждён.");
        ru.put("order_cancelled", "Заказ отменён.");
        ru.put("invalid_callback", "Некорректное действие.");
        ru.put("orders_summary", "Ваши заказы:");
        ru.put("order_status_pending", "Ожидает");
        ru.put("order_status_confirmed", "Подтверждён");
        ru.put("order_status_cancelled", "Отменён");
        ru.put("product_added_to_basket", "Продукт добавлен в корзину.");
        ru.put("basket_cleared", "Корзина очищена.");
        ru.put("enter_phone", "Пожалуйста, поделитесь своим номером телефона:");
        ru.put("enter_address", "Пожалуйста, поделитесь своим адресом:");
        ru.put("share_phone", "📞 Поделиться телефоном");
        ru.put("share_location", "📍 Поделиться местоположением");
        ru.put("invalid_input", "Неверный ввод. Пожалуйста, попробуйте снова.");
        messages.put("ru", ru);

        // English
        Map<String, String> en = new HashMap<>();
        en.put("welcome_message", "Hello! Welcome to our bot. 😊");
        en.put("language_changed", "✅ Language successfully changed!");
        en.put("select_language", "🌐 Please select your language:");
        en.put("language_button", "🌐 Change language");
        en.put("menu_button", "📁 Products");
        en.put("basket_button", "🛒 Basket");
        en.put("orders_button", "📜 My orders");
        en.put("search_button", "🔎 Search");
        en.put("back_to_menu", "⬅️ Back to menu");
        en.put("cancel_button", "❌ Cancel");
        en.put("add_to_basket", "🛒 Add to basket");
        en.put("clear_basket", "Clear basket");
        en.put("checkout", "Checkout");
        en.put("unknown_command", "Sorry, this command is not recognized.");
        en.put("error_message", "❌ An unexpected error occurred. Please try again.");
        en.put("n_orders", "You have no orders.");
        en.put("menu_message", "Category list:");
        en.put("empty_basket", "Your basket is empty.");
        en.put("enter_search_query", "Please enter your search query:");
        en.put("no_results", "No results found.");
        en.put("search_results", "Search results:");
        en.put("order_created", "Order #%d created successfully!\nAddress: %s");
        en.put("order_confirmed", "Order confirmed.");
        en.put("order_cancelled", "Order cancelled.");
        en.put("invalid_callback", "Invalid action.");
        en.put("orders_summary", "Your orders:");
        en.put("order_status_pending", "Pending");
        en.put("order_status_confirmed", "Confirmed");
        en.put("order_status_cancelled", "Cancelled");
        en.put("product_added_to_basket", "Product added to basket.");
        en.put("basket_cleared", "Basket cleared.");
        en.put("enter_phone", "Please share your phone number:");
        en.put("enter_address", "Please share your address:");
        en.put("share_phone", "📞 Share Phone");
        en.put("share_location", "📍 Share Location");
        en.put("invalid_input", "Invalid input. Please try again.");
        messages.put("en", en);
    }
}