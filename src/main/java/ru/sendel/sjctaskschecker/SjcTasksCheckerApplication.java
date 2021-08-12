package ru.sendel.sjctaskschecker;

import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
@EnableScheduling
public class SjcTasksCheckerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SjcTasksCheckerApplication.class, args);
    }

    @PostConstruct
    public void init() {
        ApiContextInitializer.init();
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        Locale.setDefault(new Locale("ru"));
    }

}
