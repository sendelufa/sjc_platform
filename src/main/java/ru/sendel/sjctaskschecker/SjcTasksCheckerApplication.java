package ru.sendel.sjctaskschecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SjcTasksCheckerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SjcTasksCheckerApplication.class, args);
    }

}
