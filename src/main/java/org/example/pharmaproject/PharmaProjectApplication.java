package org.example.pharmaproject;

import org.example.pharmaproject.bot.PharmacyBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PharmaProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmaProjectApplication.class, args);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(PharmacyBot pharmacyBot) throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(pharmacyBot);
        return botsApi;
    }
}