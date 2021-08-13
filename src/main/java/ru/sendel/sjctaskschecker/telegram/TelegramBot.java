package ru.sendel.sjctaskschecker.telegram;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sendel.sjctaskschecker.service.SolutionService;
import ru.sendel.sjctaskschecker.service.TaskService;
import ru.sendel.sjctaskschecker.view.Dashboard;

@Component
@Log4j2
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String name;

    @Value("${bot.token}")
    private String token;

    private final TelegramMessageService messageService;
    private final TaskService taskService;
    private final SolutionService solutionService;

    private final Dashboard dashboard;

    public TelegramBot(TelegramMessageService messageService,
        TaskService taskService,
        SolutionService solutionService,
        @Qualifier("DashboardMd") Dashboard dashboard) {
        this.messageService = messageService;
        this.taskService = taskService;
        this.solutionService = solutionService;
        this.dashboard = dashboard;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        String messageText = update.getMessage().getText();
        log.info("new msg:{}", messageText);

        SendMessage sendMessage ;

        if (messageText.equals("/board")) {
            log.info("telemsg: board with actual task");
            sendMessage = new SendMessage().enableMarkdown(true)
                .setText(dashboard.dashboard().replaceAll("_", "\\\\_").trim())
                .setChatId(update.getMessage().getChatId())
                .disableWebPagePreview();
        } else if (messageText.matches("/board\\s+.+")){
            log.info("telemsg: board with specific id");

            String taskId = messageText.split("\\s+")[1];
            solutionService.refreshResultOfTask(taskId);
            sendMessage = new SendMessage().enableMarkdown(true)
                .setText(dashboard.dashboard(taskService.getTaskByNumber(taskId)).replaceAll("_", "\\\\_").trim())
                .setChatId(update.getMessage().getChatId())
                .disableWebPagePreview();
        } else if(messageText.equals("/tasks")) {
           sendMessage = new SendMessage().setChatId(update.getMessage().getChatId())
            .setText(taskService.getAllTasks().toString());
        }else{
            return;
        }

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToChannel(String channelName, String text) {
        separate(text, 2000).forEach(m -> {
            log.info("\n===\n{}\n===", m.trim());
            m = m.replaceAll("_", "\\\\_");
            SendMessage sendMessage = new SendMessage().setChatId(channelName).setText(m.trim());
            sendMessage = sendMessage.enableMarkdown(true).disableWebPagePreview();
            try {
                System.out.println(execute(sendMessage).getMessageId());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });
    }

    private static List<String> separate(String text, int maxLengthMessage) {
        List<String> messagesNormal = new ArrayList<>();
        String t = text;
        while (t.length() > 0) {
            StringBuilder newMessage = new StringBuilder();
            while (newMessage.length() < maxLengthMessage && !t.isBlank()) {
                int indexOfCrlf = t.indexOf("\n");
                String line;
                if (indexOfCrlf >= 0) {
                    line = t.substring(0, indexOfCrlf + 1);
                    System.out.println(" ==>" + line);
                    t = t.substring(indexOfCrlf + 1);
                } else {
                    line = t;
                    t = "";
                }
                newMessage.append(line);
            }
            messagesNormal.add(newMessage.toString());

        }
        return messagesNormal;
    }


    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}