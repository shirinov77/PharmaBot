package org.example.pharmaproject;

import org.example.pharmaproject.bot.PharmacyBot;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class PharmaProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmaProjectApplication.class, args);
    }

    /**
     * Telegram botni fon thread’da ishga tushirish.
     */
    @Bean
    public CommandLineRunner runBot(PharmacyBot pharmacyBot) {
        return args -> {
            new Thread(() -> {
                try {
                    TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                    botsApi.registerBot(pharmacyBot);
                    System.out.println("📦 PharmacyBot ishga tushdi!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        };
    }
}
