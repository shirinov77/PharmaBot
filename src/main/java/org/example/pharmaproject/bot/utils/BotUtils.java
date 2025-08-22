package org.example.pharmaproject.bot.utils;

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

    public static InlineKeyboardMarkup createLanguageInlineKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton("🇺🇿 O‘zbekcha", "lang_uz"));
        row.add(createButton("🇷🇺 Русский", "lang_ru"));
        row.add(createButton("🇬🇧 English", "lang_en"));
        rows.add(row);

        markup.setKeyboard(rows);
        return markup;
    }

    public static InlineKeyboardMarkup createCategoryInlineKeyboard(List<Category> categories, String lang) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Category category : categories) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(createButton(category.getName(), "category_" + category.getId()));
            rows.add(row);
        }

        markup.setKeyboard(rows);
        return markup;
    }

    public static InlineKeyboardMarkup createProductsInlineKeyboard(List<Product> products, String lang) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Product product : products) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(createButton(product.getName(), "product_" + product.getId() + "_details"));
            row.add(createButton(getLocalizedMessage(lang, "add_to_basket"), "product_" + product.getId() + "_add"));
            rows.add(row);
        }

        markup.setKeyboard(rows);
        return markup;
    }

    public static InlineKeyboardMarkup createBasketInlineKeyboard(List<Product> products, String lang) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Product product : products) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(createButton(product.getName(), "product_" + product.getId() + "_details"));
            row.add(createButton(getLocalizedMessage(lang, "remove"), "basket_remove_" + product.getId()));
            rows.add(row);
        }

        List<InlineKeyboardButton> actionRow = new ArrayList<>();
        actionRow.add(createButton(getLocalizedMessage(lang, "clear_basket"), "basket_clear"));
        actionRow.add(createButton(getLocalizedMessage(lang, "checkout"), "basket_checkout"));
        rows.add(actionRow);

        markup.setKeyboard(rows);
        return markup;
    }

    public static InlineKeyboardMarkup createOrdersInlineKeyboard(List<Order> orders, String lang) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Order order : orders) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            // Order statusni string qilib olish (enum bo‘lsa ham, string bo‘lsa ham ishlaydi)
            String statusValue = order.getStatus() != null ? order.getStatus().toString() : "UNKNOWN";
            String statusKey = statusValue.toLowerCase();

            // Localized text olish
            String statusText = getLocalizedMessage(lang, "order_status_" + statusKey);

            // Order tugmasi
            String orderText = String.format("#%d (%s)", order.getId(), statusText);
            row.add(createButton(orderText, "order_" + order.getId() + "_details"));

            // Agar PENDING bo‘lsa, confirm va cancel tugmalarini qo‘shish
            if ("PENDING".equalsIgnoreCase(statusValue)) {
                row.add(createButton(getLocalizedMessage(lang, "confirm_order"), "order_" + order.getId() + "_confirm"));
                row.add(createButton(getLocalizedMessage(lang, "cancel_order"), "order_" + order.getId() + "_cancel"));
            }

            rows.add(row);
        }

        markup.setKeyboard(rows);
        return markup;
    }

    public static InlineKeyboardMarkup createOrderActionsInline(Long orderId, String lang) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton(getLocalizedMessage(lang, "confirm_order"), "order_" + orderId + "_confirm"));
        row.add(createButton(getLocalizedMessage(lang, "cancel_order"), "order_" + orderId + "_cancel"));
        rows.add(row);

        markup.setKeyboard(rows);
        return markup;
    }

    public static InlineKeyboardMarkup createAddToBasketInline(String productId, String lang) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton(getLocalizedMessage(lang, "add_to_basket"), "product_" + productId + "_add"));
        rows.add(row);

        markup.setKeyboard(rows);
        return markup;
    }

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

    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public static String getLocalizedMessage(String lang, String key) {
        return switch (key) {
            case "welcome_message" -> switch (lang) {
                case "uz" -> "Salom 👋 Apteka botiga xush kelibsiz!\n\nIltimos, tilni tanlang:";
                case "ru" -> "Здравствуйте 👋 Добро пожаловать в бот аптеки!\n\nПожалуйста, выберите язык:";
                case "en" -> "Hello 👋 Welcome to the Pharmacy Bot!\n\nPlease select a language:";
                default -> "Welcome!";
            };
            case "select_language" -> switch (lang) {
                case "uz" -> "Iltimos, tilni tanlang:";
                case "ru" -> "Пожалуйста, выберите язык:";
                case "en" -> "Please select a language:";
                default -> "Select language:";
            };
            case "language_changed" -> switch (lang) {
                case "uz" -> "Til o‘zgartirildi! Endi asosiy menyudan foydalaning.";
                case "ru" -> "Язык изменён! Используйте главное меню.";
                case "en" -> "Language changed! Use the main menu.";
                default -> "Language changed!";
            };
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
            case "menu" -> switch (lang) {
                case "uz" -> "Menyu";
                case "ru" -> "Меню";
                case "en" -> "Menu";
                default -> "Menu";
            };
            case "search" -> switch (lang) {
                case "uz" -> "Qidirish";
                case "ru" -> "Поиск";
                case "en" -> "Search";
                default -> "Search";
            };
            case "basket" -> switch (lang) {
                case "uz" -> "Savat";
                case "ru" -> "Корзина";
                case "en" -> "Basket";
                default -> "Basket";
            };
            case "orders" -> switch (lang) {
                case "uz" -> "Buyurtmalarim";
                case "ru" -> "Мои заказы";
                case "en" -> "My Orders";
                default -> "My Orders";
            };
            case "change_language" -> switch (lang) {
                case "uz" -> "Tilni o'zgartirish";
                case "ru" -> "Сменить язык";
                case "en" -> "Change Language";
                default -> "Change Language";
            };
            case "change_address" -> switch (lang) {
                case "uz" -> "Manzilni o'zgartirish";
                case "ru" -> "Сменить адрес";
                case "en" -> "Change Address";
                default -> "Change Address";
            };
            case "add_to_basket" -> switch (lang) {
                case "uz" -> "Savatga qo‘shish";
                case "ru" -> "Добавить в корзину";
                case "en" -> "Add to Basket";
                default -> "Add to Basket";
            };
            case "remove" -> switch (lang) {
                case "uz" -> "O‘chirish";
                case "ru" -> "Удалить";
                case "en" -> "Remove";
                default -> "Remove";
            };
            case "clear_basket" -> switch (lang) {
                case "uz" -> "Savatni tozalash";
                case "ru" -> "Очистить корзину";
                case "en" -> "Clear Basket";
                default -> "Clear Basket";
            };
            case "checkout" -> switch (lang) {
                case "uz" -> "Buyurtma qilish";
                case "ru" -> "Оформить заказ";
                case "en" -> "Checkout";
                default -> "Checkout";
            };
            case "confirm_order" -> switch (lang) {
                case "uz" -> "Tasdiqlash";
                case "ru" -> "Подтвердить";
                case "en" -> "Confirm";
                default -> "Confirm";
            };
            case "cancel_order" -> switch (lang) {
                case "uz" -> "Bekor qilish";
                case "ru" -> "Отменить";
                case "en" -> "Cancel";
                default -> "Cancel";
            };
            case "menu_message" -> switch (lang) {
                case "uz" -> "📋 Menyu:\nKategoriyalarni tanlang:";
                case "ru" -> "📋 Меню:\nВыберите категорию:";
                case "en" -> "📋 Menu:\nSelect a category:";
                default -> "Select a category";
            };
            case "category_selected" -> switch (lang) {
                case "uz" -> "Tanlangan kategoriya: ";
                case "ru" -> "Выбранная категория: ";
                case "en" -> "Selected category: ";
                default -> "Selected category: ";
            };
            case "search_results" -> switch (lang) {
                case "uz" -> "🔍 Qidiruv natijalari: ";
                case "ru" -> "🔍 Результаты поиска: ";
                case "en" -> "🔍 Search results: ";
                default -> "Search results: ";
            };
            case "no_results" -> switch (lang) {
                case "uz" -> "❌ Hech narsa topilmadi.";
                case "ru" -> "❌ Ничего не найдено.";
                case "en" -> "❌ No results found.";
                default -> "No results found.";
            };
            case "product_details" -> switch (lang) {
                case "uz" -> "Mahsulot: %s\nNarxi: %s\nMiqdori: %d";
                case "ru" -> "Товар: %s\nЦена: %s\nКоличество: %d";
                case "en" -> "Product: %s\nPrice: %s\nQuantity: %d";
                default -> "Product: %s\nPrice: %s\nQuantity: %d";
            };
            case "basket_summary" -> switch (lang) {
                case "uz" -> "🛒 Savatingiz:";
                case "ru" -> "🛒 Ваша корзина:";
                case "en" -> "🛒 Your basket:";
                default -> "Your basket:";
            };
            case "empty_basket" -> switch (lang) {
                case "uz" -> "🛒 Savatingiz bo‘sh.";
                case "ru" -> "🛒 Ваша корзина пуста.";
                case "en" -> "🛒 Your basket is empty.";
                default -> "Your basket is empty.";
            };
            case "added_to_basket" -> switch (lang) {
                case "uz" -> "✅ Savatga qo‘shildi: ";
                case "ru" -> "✅ Добавлено в корзину: ";
                case "en" -> "✅ Added to basket: ";
                default -> "Added to basket: ";
            };
            case "removed_from_basket" -> switch (lang) {
                case "uz" -> "🗑 Savatdan o‘chirildi.";
                case "ru" -> "🗑 Удалено из корзины.";
                case "en" -> "🗑 Removed from basket.";
                default -> "Removed from basket.";
            };
            case "basket_cleared" -> switch (lang) {
                case "uz" -> "🛒 Savat tozalandi.";
                case "ru" -> "🛒 Корзина очищена.";
                case "en" -> "🛒 Basket cleared.";
                default -> "Basket cleared.";
            };
            case "total_price" -> switch (lang) {
                case "uz" -> "\nUmumiy narx: %s";
                case "ru" -> "\nОбщая сумма: %s";
                case "en" -> "\nTotal price: %s";
                default -> "\nTotal price: %s";
            };
            case "orders_summary" -> switch (lang) {
                case "uz" -> "📦 Sizning buyurtmalaringiz:";
                case "ru" -> "📦 Ваши заказы:";
                case "en" -> "📦 Your orders:";
                default -> "Your orders:";
            };
            case "no_orders" -> switch (lang) {
                case "uz" -> "❌ Hozircha buyurtmalar yo‘q.";
                case "ru" -> "❌ У вас пока нет заказов.";
                case "en" -> "❌ No orders yet.";
                default -> "No orders yet.";
            };
            case "order_created" -> switch (lang) {
                case "uz" -> "✅ Buyurtma yaratildi: #";
                case "ru" -> "✅ Заказ создан: #";
                case "en" -> "✅ Order created: #";
                default -> "Order created: #";
            };
            case "order_confirmed" -> switch (lang) {
                case "uz" -> "✅ Buyurtma tasdiqlandi.";
                case "ru" -> "✅ Заказ подтверждён.";
                case "en" -> "✅ Order confirmed.";
                default -> "Order confirmed.";
            };
            case "order_cancelled" -> switch (lang) {
                case "uz" -> "❌ Buyurtma bekor qilindi.";
                case "ru" -> "❌ Заказ отменён.";
                case "en" -> "❌ Order cancelled.";
                default -> "Order cancelled.";
            };
            case "order_status_pending" -> switch (lang) {
                case "uz" -> "Kutilmoqda";
                case "ru" -> "В ожидании";
                case "en" -> "Pending";
                default -> "Pending";
            };
            case "order_status_confirmed" -> switch (lang) {
                case "uz" -> "Tasdiqlangan";
                case "ru" -> "Подтверждён";
                case "en" -> "Confirmed";
                default -> "Confirmed";
            };
            case "order_status_cancelled" -> switch (lang) {
                case "uz" -> "Bekor qilingan";
                case "ru" -> "Отменён";
                case "en" -> "Cancelled";
                default -> "Cancelled";
            };
            default -> "Unknown message";
        };
    }
}