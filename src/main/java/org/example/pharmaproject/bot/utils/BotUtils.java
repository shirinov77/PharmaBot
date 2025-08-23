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


    /**
     * 🌐 Til tanlash inline keyboard
     */
    public static InlineKeyboardMarkup createLanguageInlineKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(createButton("🇺🇿 O‘zbekcha", "lang_uz"));
        row.add(createButton("🇷🇺 Русский", "lang_ru"));
        row.add(createButton("🇬🇧 English", "lang_en"));
        rows.add(row);

        return new InlineKeyboardMarkup(rows);
    }

    /**
     * 📂 Kategoriya inline keyboard
     */
    public static InlineKeyboardMarkup createCategoryInlineKeyboard(List<Category> categories, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Category category : categories) {
            rows.add(List.of(createButton(category.getName(), "category_" + category.getId())));
        }
        rows.add(List.of(createButton(getLocalizedMessage(lang, "back_to_menu"), "back_to_menu")));

        return new InlineKeyboardMarkup(rows);
    }

    /**
     * 💊 Mahsulotlar ro‘yxati inline keyboard
     */
    public static InlineKeyboardMarkup createProductsInlineKeyboard(List<Product> products, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Product product : products) {
            rows.add(List.of(
                    createButton(product.getName(), "product_" + product.getId())
            ));
        }

        rows.add(List.of(createButton(getLocalizedMessage(lang, "back"), "back")));
        return new InlineKeyboardMarkup(rows);
    }

    /**
     * ℹ️ Mahsulot tafsilotlari inline keyboard
     */
    public static InlineKeyboardMarkup createProductDetailsInline(String productId, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(createButton(getLocalizedMessage(lang, "add_to_basket"), "add_to_basket_" + productId)));
        rows.add(List.of(createButton(getLocalizedMessage(lang, "back"), "back")));
        return new InlineKeyboardMarkup(rows);
    }

    /**
     * 🛒 Savatni boshqarish inline keyboard
     */
    public static InlineKeyboardMarkup createBasketManagementKeyboard(Basket basket, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        if (basket.getProducts() != null && !basket.getProducts().isEmpty()) {
            for (Product product : basket.getProducts()) {
                rows.add(List.of(
                        createButton("➖", "decrease_product_count_" + product.getId()),
                        createButton(product.getName() + " (" + product.getQuantityInBasket() + ")", "product_details_" + product.getId()),
                        createButton("➕", "increase_product_count_" + product.getId())
                ));
            }

            rows.add(List.of(
                    createButton(getLocalizedMessage(lang, "clear_basket"), "basket_clear"),
                    createButton(getLocalizedMessage(lang, "checkout"), "basket_checkout")
            ));
        }

        rows.add(List.of(createButton(getLocalizedMessage(lang, "back_to_menu"), "back_to_menu")));

        return new InlineKeyboardMarkup(rows);
    }

    /**
     * 📦 Buyurtmalar ro‘yxati inline keyboard
     */
    public static InlineKeyboardMarkup createOrdersInlineKeyboard(List<Order> orders, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Order order : orders) {
            String statusValue = order.getStatus() != null ? order.getStatus().toString().toLowerCase() : "unknown";
            String buttonText = String.format("#%s | %s", order.getId(), getLocalizedMessage(lang, "order_status_" + statusValue));
            rows.add(List.of(createButton(buttonText, "order_details_" + order.getId())));
        }

        rows.add(List.of(createButton(getLocalizedMessage(lang, "back_to_menu"), "back_to_menu")));
        return new InlineKeyboardMarkup(rows);
    }

    /**
     * Umumiy inline tugma yaratuvchi yordamchi metod
     */
    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }


    /* ======================= REPLY KEYBOARDLAR ======================= */

    /**
     * 🏠 Asosiy menyu keyboard
     */
    public static ReplyKeyboardMarkup getMainKeyboard(String lang) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(getLocalizedMessage(lang, "menu"));
        row1.add(getLocalizedMessage(lang, "search"));
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(getLocalizedMessage(lang, "basket"));
        row2.add(getLocalizedMessage(lang, "my_orders"));
        keyboard.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(getLocalizedMessage(lang, "change_language"));
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        return keyboardMarkup;
    }

    /**
     * Qidiruvdan qaytish uchun keyboard
     */
    public static ReplyKeyboardMarkup createBackToMenuKeyboard(String lang) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(getLocalizedMessage(lang, "back_to_menu"));
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    /* ======================= LOKALIZATSIYA MATNLARI ======================= */

    /**
     * Tilga qarab matn qaytaruvchi metod
     */
    public static String getLocalizedMessage(String lang, String key) {
        return switch (key) {
            // WELCOME
            case "select_language" -> switch (lang) {
                case "uz" -> "🌐 Iltimos, tilni tanlang:";
                case "ru" -> "🌐 Пожалуйста, выберите язык:";
                case "en" -> "🌐 Please select a language:";
                default -> "Please select a language:";
            };
            case "language_changed" -> switch (lang) {
                case "uz" -> "✅ Til muvaffaqiyatli o‘zgartirildi!";
                case "ru" -> "✅ Язык успешно изменен!";
                case "en" -> "✅ Language successfully changed!";
                default -> "Language successfully changed!";
            };
            case "welcome_message" -> switch (lang) {
                case "uz" -> "Assalomu alaykum! Asosiy menyudan tanlang.";
                case "ru" -> "Здравствуйте! Выберите в главном меню.";
                case "en" -> "Hello! Please select from the main menu.";
                default -> "Hello!";
            };

            // MAIN MENU
            case "menu" -> switch (lang) {
                case "uz" -> "📂 Menyu";
                case "ru" -> "📂 Меню";
                case "en" -> "📂 Menu";
                default -> "Menu";
            };
            case "search" -> switch (lang) {
                case "uz" -> "🔎 Qidiruv";
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
            case "my_orders" -> switch (lang) {
                case "uz" -> "📦 Buyurtmalarim";
                case "ru" -> "📦 Мои заказы";
                case "en" -> "📦 My orders";
                default -> "My orders";
            };
            case "change_language" -> switch (lang) {
                case "uz" -> "🇺🇿 Tilni o‘zgartirish";
                case "ru" -> "🇷🇺 Сменить язык";
                case "en" -> "🇬🇧 Change language";
                default -> "Change language";
            };

            // MENU & PRODUCT
            case "menu_message" -> switch (lang) {
                case "uz" -> "Kategoriyalardan birini tanlang:";
                case "ru" -> "Выберите одну из категорий:";
                case "en" -> "Select one of the categories:";
                default -> "Select one of the categories:";
            };
            case "category_selected" -> switch (lang) {
                case "uz" -> "✅ Kategoriya tanlandi: ";
                case "ru" -> "✅ Категория выбрана: ";
                case "en" -> "✅ Category selected: ";
                default -> "Category selected: ";
            };
            case "no_products" -> switch (lang) {
                case "uz" -> "Bu kategoriyada mahsulotlar mavjud emas.";
                case "ru" -> "В этой категории нет товаров.";
                case "en" -> "No products available in this category.";
                default -> "No products available in this category.";
            };
            case "products_list" -> switch (lang) {
                case "uz" -> "Mahsulotlardan birini tanlang:";
                case "ru" -> "Выберите один из товаров:";
                case "en" -> "Select one of the products:";
                default -> "Select one of the products:";
            };
            case "product_details" -> switch (lang) {
                case "uz" -> "<b>Mahsulot:</b> %s\n<b>Narxi:</b> %s\n<b>Mavjud:</b> %s dona\n\n<b>Tavsif:</b> %s";
                case "ru" -> "<b>Товар:</b> %s\n<b>Цена:</b> %s\n<b>В наличии:</b> %s шт\n\n<b>Описание:</b> %s";
                case "en" -> "<b>Product:</b> %s\n<b>Price:</b> %s\n<b>Available:</b> %s pcs\n\n<b>Description:</b> %s";
                default -> "<b>Product:</b> %s\n<b>Price:</b> %s\n<b>Available:</b> %s pcs\n\n<b>Description:</b> %s";
            };
            case "add_to_basket" -> switch (lang) {
                case "uz" -> "🛒 Savatga qo‘shish";
                case "ru" -> "🛒 Добавить в корзину";
                case "en" -> "🛒 Add to basket";
                default -> "Add to basket";
            };

            // SEARCH
            case "enter_search_query" -> switch (lang) {
                case "uz" -> "🔎 Iltimos, qidiruv so‘rovini kiriting:";
                case "ru" -> "🔎 Пожалуйста, введите поисковый запрос:";
                case "en" -> "🔎 Please enter your search query:";
                default -> "Enter search query:";
            };
            case "search_results" -> switch (lang) {
                case "uz" -> "🔎 Qidiruv natijalari:";
                case "ru" -> "🔎 Результаты поиска:";
                case "en" -> "🔎 Search results:";
                default -> "Search results:";
            };
            case "no_results" -> switch (lang) {
                case "uz" -> "❌ Afsuski, sizning so‘rovingiz bo‘yicha hech narsa topilmadi.";
                case "ru" -> "❌ К сожалению, по вашему запросу ничего не найдено.";
                case "en" -> "❌ Unfortunately, nothing was found for your query.";
                default -> "No results found.";
            };

            // BASKET
            case "empty_basket" -> switch (lang) {
                case "uz" -> "🛒 Savatingiz bo‘sh.";
                case "ru" -> "🛒 Ваша корзина пуста.";
                case "en" -> "🛒 Your basket is empty.";
                default -> "Your basket is empty.";
            };
            case "basket_summary" -> switch (lang) {
                case "uz" -> "🛒 Savatingizdagi mahsulotlar:";
                case "ru" -> "🛒 Товары в вашей корзине:";
                case "en" -> "🛒 Products in your basket:";
                default -> "Products in your basket:";
            };
            case "total_price" -> switch (lang) {
                case "uz" -> "Umumiy narx: <b>%s</b>";
                case "ru" -> "Итого: <b>%s</b>";
                case "en" -> "Total price: <b>%s</b>";
                default -> "Total price: <b>%s</b>";
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
            case "basket_cleared" -> switch (lang) {
                case "uz" -> "🗑 Savatingiz tozalandi.";
                case "ru" -> "🗑 Ваша корзина очищена.";
                case "en" -> "🗑 Your basket has been cleared.";
                default -> "Your basket has been cleared.";
            };
            case "product_added_to_basket" -> switch (lang) {
                case "uz" -> "✅ Mahsulot savatga qo‘shildi.";
                case "ru" -> "✅ Товар добавлен в корзину.";
                case "en" -> "✅ Product added to basket.";
                default -> "Product added to basket.";
            };

            // ORDER
            case "n_orders" -> switch (lang) {
                case "uz" -> "📦 Sizda hali buyurtmalar mavjud emas.";
                case "ru" -> "📦 У вас пока нет заказов.";
                case "en" -> "📦 You don't have any orders yet.";
                default -> "You don't have any orders yet.";
            };
            case "orders_summary" -> switch (lang) {
                case "uz" -> "📦 Buyurtmalaringiz ro‘yxati:";
                case "ru" -> "📦 Список ваших заказов:";
                case "en" -> "📦 List of your orders:";
                default -> "List of your orders:";
            };
            case "order_status_pending" -> switch (lang) {
                case "uz" -> "Kutilmoqda";
                case "ru" -> "Ожидает";
                case "en" -> "Pending";
                default -> "Pending";
            };
            case "order_status_confirmed" -> switch (lang) {
                case "uz" -> "Tasdiqlandi";
                case "ru" -> "Подтвержден";
                case "en" -> "Confirmed";
                default -> "Confirmed";
            };
            case "order_status_delivered" -> switch (lang) {
                case "uz" -> "Yetkazildi";
                case "ru" -> "Доставлен";
                case "en" -> "Delivered";
                default -> "Delivered";
            };
            case "order_status_cancelled" -> switch (lang) {
                case "uz" -> "Bekor qilindi";
                case "ru" -> "Отменен";
                case "en" -> "Cancelled";
                default -> "Cancelled";
            };
            case "enter_address" -> switch (lang) {
                case "uz" -> "🚚 Buyurtmani yakunlash uchun, iltimos, manzilingizni kiriting:";
                case "ru" -> "🚚 Для завершения заказа, пожалуйста, введите ваш адрес:";
                case "en" -> "🚚 To complete the order, please enter your address:";
                default -> "Please enter your address:";
            };
            case "order_created" -> switch (lang) {
                case "uz" -> "✅ Buyurtmangiz qabul qilindi. Buyurtma raqami: <b>#%d</b>. Manzil: %s";
                case "ru" -> "✅ Ваш заказ принят. Номер заказа: <b>#%d</b>. Адрес: %s";
                case "en" -> "✅ Your order has been accepted. Order number: <b>#%d</b>. Address: %s";
                default -> "Your order has been accepted. Order number: <b>#%d</b>. Address: %s";
            };
            case "order_confirmed" -> switch (lang) {
                case "uz" -> "✅ Buyurtma tasdiqlandi.";
                case "ru" -> "✅ Заказ подтвержден.";
                case "en" -> "✅ Order confirmed.";
                default -> "Order confirmed.";
            };
            case "order_cancelled" -> switch (lang) {
                case "uz" -> "❌ Buyurtma bekor qilindi.";
                case "ru" -> "❌ Заказ отменен.";
                case "en" -> "❌ Order cancelled.";
                default -> "Order cancelled.";
            };

            // BACK & CANCEL
            case "back" -> switch (lang) {
                case "uz" -> "⬅️ Orqaga";
                case "ru" -> "⬅️ Назад";
                case "en" -> "⬅️ Back";
                default -> "Back";
            };
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

            // GENERAL
            case "unknown_command" -> switch (lang) {
                case "uz" -> "❓ Noma'lum buyruq. Iltimos, menyudan foydalaning.";
                case "ru" -> "❓ Неизвестная команда. Пожалуйста, используйте меню.";
                case "en" -> "❓ Unknown command. Please use the menu.";
                default -> "Unknown command.";
            };
            case "invalid_callback" -> switch (lang) {
                case "uz" -> "Xatolik yuz berdi. Iltimos, asosiy menyuga qayting.";
                case "ru" -> "Произошла ошибка. Пожалуйста, вернитесь в главное меню.";
                case "en" -> "An error occurred. Please return to the main menu.";
                default -> "Error.";
            };

            // DEFAULT
            default -> "Unknown message";
        };
    }
}