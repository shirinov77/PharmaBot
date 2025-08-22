package org.example.pharmaproject.bot.utils;

import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.Category;
import org.example.pharmaproject.entities.Order;
import org.example.pharmaproject.entities.Product;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class BotUtils {

    /* ======================= INLINE KEYBOARDLAR ======================= */

    /** 🌐 Til tanlash keyboard */
    public static InlineKeyboardMarkup createLanguageInlineKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(createButton("🇺🇿 O‘zbekcha", "lang_uz"));
        row.add(createButton("🇷🇺 Русский", "lang_ru"));
        row.add(createButton("🇬🇧 English", "lang_en"));
        rows.add(row);

        return new InlineKeyboardMarkup(rows);
    }

    /** 📂 Kategoriya keyboard */
    public static InlineKeyboardMarkup createCategoryInlineKeyboard(List<Category> categories, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Category category : categories) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(createButton(category.getName(), "category_" + category.getId()));
            rows.add(row);
        }

        return new InlineKeyboardMarkup(rows);
    }

    /** 🛒 Mahsulotlar ro‘yxati keyboard */
    public static InlineKeyboardMarkup createProductsInlineKeyboard(List<Product> products, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Product product : products) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(createButton(product.getName(), "product_" + product.getId() + "_details"));
            row.add(createButton(getLocalizedMessage(lang, "add_to_basket"), "product_" + product.getId() + "_add"));
            rows.add(row);
        }

        return new InlineKeyboardMarkup(rows);
    }

    /** Savatdagi mahsulotlar uchun keyboard */
    public static InlineKeyboardMarkup createBasketInlineKeyboard(List<Product> products, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Product product : products) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(createButton(product.getName(), "product_" + product.getId() + "_details"));
            row.add(createButton(getLocalizedMessage(lang, "remove"), "basket_remove_" + product.getId()));
            rows.add(row);
        }

        // Yakuniy tugmalar
        List<InlineKeyboardButton> actionRow = new ArrayList<>();
        actionRow.add(createButton(getLocalizedMessage(lang, "clear_basket"), "basket_clear"));
        actionRow.add(createButton(getLocalizedMessage(lang, "checkout"), "basket_checkout"));
        rows.add(actionRow);

        return new InlineKeyboardMarkup(rows);
    }

    /** Buyurtmalar keyboard */
    public static InlineKeyboardMarkup createOrdersInlineKeyboard(List<Order> orders, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Order order : orders) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(createButton("#" + order.getId() + " (" + order.getStatus() + ")", "order_" + order.getId() + "_details"));

            if ("PENDING".equals(order.getStatus())) {
                row.add(createButton(getLocalizedMessage(lang, "confirm_order"), "order_" + order.getId() + "_confirm"));
                row.add(createButton(getLocalizedMessage(lang, "cancel_order"), "order_" + order.getId() + "_cancel"));
            }
            rows.add(row);
        }

        return new InlineKeyboardMarkup(rows);
    }

    /** 🔙 Asosiy menyuga qaytish */
    public static InlineKeyboardMarkup createBackToMenuKeyboard(String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(createButton(getLocalizedMessage(lang, "back_to_menu"), "back_to_menu"));
        rows.add(row);

        return new InlineKeyboardMarkup(rows);
    }

    /** 📦 Mahsulot tafsilotlari uchun tugmalar */
    public static InlineKeyboardMarkup createProductDetailsInline(String productId, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(createButton(getLocalizedMessage(lang, "add_to_basket"), "product_" + productId + "_add")));
        rows.add(List.of(createButton(getLocalizedMessage(lang, "back_to_menu"), "back_to_menu")));

        return new InlineKeyboardMarkup(rows);
    }

    /** ✅❌ Buyurtma tasdiqlash yoki bekor qilish */
    public static InlineKeyboardMarkup createOrderActionsInline(Long orderId, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                createButton(getLocalizedMessage(lang, "confirm_order"), "order_" + orderId + "_confirm"),
                createButton(getLocalizedMessage(lang, "cancel_order"), "order_" + orderId + "_cancel")
        ));

        return new InlineKeyboardMarkup(rows);
    }

    /* ======================= REPLY KEYBOARD ======================= */

    public static ReplyKeyboardMarkup getMainKeyboard(String lang) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(getLocalizedMessage(lang, "menu"));
        row1.add(getLocalizedMessage(lang, "search"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(getLocalizedMessage(lang, "basket"));
        row2.add(getLocalizedMessage(lang, "orders"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(getLocalizedMessage(lang, "change_language"));
        row3.add(getLocalizedMessage(lang, "change_address"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    /* ======================= YORDAMCHI ======================= */

    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    /* ======================= TARJIMALAR ======================= */
    public static String getLocalizedMessage(String lang, String key) {
        return switch (key) {
            // START
            case "welcome_message" -> switch (lang) {
                case "uz" -> "👋 Salom, aptekaga xush kelibsiz!\nQuyidagi menyudan foydalaning:";
                case "ru" -> "👋 Здравствуйте, добро пожаловать в аптеку!\nВыберите действие ниже:";
                case "en" -> "👋 Hello, welcome to the pharmacy!\nPlease use the menu below:";
                default -> "Welcome!";
            };
            case "select_language" -> switch (lang) {
                case "uz" -> "Iltimos, tilni tanlang:";
                case "ru" -> "Пожалуйста, выберите язык:";
                case "en" -> "Please select a language:";
                default -> "Select language:";
            };
            case "language_changed" -> switch (lang) {
                case "uz" -> "✅ Til muvaffaqiyatli o‘zgartirildi!";
                case "ru" -> "✅ Язык успешно изменён!";
                case "en" -> "✅ Language successfully changed!";
                default -> "Language changed!";
            };

            // MENU
            case "menu" -> switch (lang) {
                case "uz" -> "📋 Menyu";
                case "ru" -> "📋 Меню";
                case "en" -> "📋 Menu";
                default -> "Menu";
            };
            case "search" -> switch (lang) {
                case "uz" -> "🔎 Qidirish";
                case "ru" -> "🔎 Поиск";
                case "en" -> "🔎 Search";
                default -> "Search";
            };
            case "basket" -> switch (lang) {
                case "uz" -> "🛒 Savat";
                case "ru" -> "🛒 Корзина";
                case "en" -> "🛒 Basket";
                default -> "Basket";
            };
            case "orders" -> switch (lang) {
                case "uz" -> "📦 Buyurtmalar";
                case "ru" -> "📦 Заказы";
                case "en" -> "📦 Orders";
                default -> "Orders";
            };
            case "change_language" -> switch (lang) {
                case "uz" -> "🌐 Tilni o‘zgartirish";
                case "ru" -> "🌐 Сменить язык";
                case "en" -> "🌐 Change language";
                default -> "Change language";
            };
            case "change_address" -> switch (lang) {
                case "uz" -> "📍 Manzilni o‘zgartirish";
                case "ru" -> "📍 Изменить адрес";
                case "en" -> "📍 Change address";
                default -> "Change address";
            };

            // BASKET
            case "add_to_basket" -> switch (lang) {
                case "uz" -> "➕ Savatga qo‘shish";
                case "ru" -> "➕ Добавить в корзину";
                case "en" -> "➕ Add to basket";
                default -> "Add to basket";
            };
            case "remove" -> switch (lang) {
                case "uz" -> "❌ O‘chirish";
                case "ru" -> "❌ Удалить";
                case "en" -> "❌ Remove";
                default -> "Remove";
            };
            case "clear_basket" -> switch (lang) {
                case "uz" -> "🗑 Savatni tozalash";
                case "ru" -> "🗑 Очистить корзину";
                case "en" -> "🗑 Clear basket";
                default -> "Clear basket";
            };
            case "checkout" -> switch (lang) {
                case "uz" -> "✅ Buyurtma berish";
                case "ru" -> "✅ Оформить заказ";
                case "en" -> "✅ Checkout";
                default -> "Checkout";
            };

            // ORDERS
            case "confirm_order" -> switch (lang) {
                case "uz" -> "✅ Tasdiqlash";
                case "ru" -> "✅ Подтвердить";
                case "en" -> "✅ Confirm";
                default -> "Confirm";
            };
            case "cancel_order" -> switch (lang) {
                case "uz" -> "❌ Bekor qilish";
                case "ru" -> "❌ Отменить";
                case "en" -> "❌ Cancel";
                default -> "Cancel";
            };

            // SEARCH
            case "no_results" -> switch (lang) {
                case "uz" -> "❌ Hech narsa topilmadi";
                case "ru" -> "❌ Ничего не найдено";
                case "en" -> "❌ No results found";
                default -> "No results";
            };
            case "search_results" -> switch (lang) {
                case "uz" -> "🔎 Qidiruv natijalari: ";
                case "ru" -> "🔎 Результаты поиска: ";
                case "en" -> "🔎 Search results: ";
                default -> "Results: ";
            };
            case "product_details" -> switch (lang) {
                case "uz" -> "%s\n💵 Narxi: %s\n📦 Soni: %d dona";
                case "ru" -> "%s\n💵 Цена: %s\n📦 Кол-во: %d шт";
                case "en" -> "%s\n💵 Price: %s\n📦 Quantity: %d pcs";
                default -> "%s - %s";
            };

            // BACK
            case "back_to_menu" -> switch (lang) {
                case "uz" -> "⬅️ Asosiy menyuga qaytish";
                case "ru" -> "⬅️ Вернуться в меню";
                case "en" -> "⬅️ Back to menu";
                default -> "Back to menu";
            };
            case "cancel" -> switch (lang) {
                case "uz" -> "❌ Bekor qilish";
                case "ru" -> "❌ Отменить";
                case "en" -> "❌ Cancel";
                default -> "Cancel";
            };

            // DEFAULT
            default -> "Unknown message";
        };
    }

    /* ======================= SAVAT MANAGEMENT ======================= */

    public static InlineKeyboardMarkup createBasketManagementKeyboard(Basket basket, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        if (basket.getProducts() != null && !basket.getProducts().isEmpty()) {
            for (Product product : basket.getProducts()) {
                rows.add(List.of(createButton(product.getName() + " ❌", "basket_remove_" + product.getId())));
            }

            rows.add(List.of(
                    createButton(getLocalizedMessage(lang, "clear_basket"), "basket_clear"),
                    createButton(getLocalizedMessage(lang, "checkout"), "basket_checkout")
            ));
        } else {
            rows.add(List.of(createButton(getLocalizedMessage(lang, "back_to_menu"), "back_to_menu")));
        }

        return new InlineKeyboardMarkup(rows);
    }
}
