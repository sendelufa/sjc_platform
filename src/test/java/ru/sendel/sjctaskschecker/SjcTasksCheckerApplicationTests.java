package ru.sendel.sjctaskschecker;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.telegram.telegrambots.starter.TelegramBotInitializer;
import ru.sendel.sjctaskschecker.telegram.TelegramBot;

@SpringBootTest
@ActiveProfiles(profiles = "test")
class SjcTasksCheckerApplicationTests {

    @MockBean
    private TelegramBot telegramBot;

    @MockBean
    private TelegramBotInitializer telegramBotInitializer;

    @SneakyThrows
    @BeforeEach
    void setup(){
        telegramBot = mock(TelegramBot.class);
        doNothing().when(telegramBot).clearWebhook();
    }

    @Test
    void contextLoads() {
    }

}
