package ru.sendel.sjctaskschecker.telegram;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
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

    Map<String, Integer> taskToMessageMap = new ConcurrentHashMap<>();

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
        if (!update.hasMessage() || update.getMessage().getText() == null) {
            return;
        }

        String messageText = update.getMessage().getText();
        log.info("new msg:{} from {} {}", messageText, update.getMessage().getChatId(),
            update.getMessage().getChat().getUserName());

        SendMessage sendMessage;

        if (messageText.equals("/board")) {
            log.info("telemsg: board with actual task");
            sendMessage = new SendMessage().enableMarkdown(true)
                .setText(dashboard.dashboard().replaceAll("_", "\\\\_").trim())
                .setChatId(update.getMessage().getChatId())
                .disableWebPagePreview();
        } else if (messageText.matches("/board\\s+.+")) {
            log.info("telemsg: board with specific id");

            String taskId = messageText.split("\\s+")[1];
            try {
                solutionService.refreshResultOfTask(taskId);
                sendMessage = new SendMessage().enableMarkdown(true)
                    .setText(dashboard.dashboard(taskService.getTaskByNumber(taskId))
                        .replaceAll("_", "\\\\_").trim())
                    .setChatId(update.getMessage().getChatId())
                    .disableWebPagePreview();
            } catch (NoSuchElementException e) {
                log.error(e);
                sendMessage = new SendMessage().setText("Задание не найдено")
                    .setChatId(update.getMessage().getChatId());

            }
        } else if (messageText.equals("/tasks")) {
            sendMessage = new SendMessage().setChatId(update.getMessage().getChatId())
                .setText(taskService.getAllTasks().toString().replaceAll("[,\\]\\[]", ""))
                .enableMarkdown(true).disableWebPagePreview();
        } else {
            return;
        }

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public Message sendMessageToChannel(String channelName, String text) {
        text = text.replaceAll("_", "\\\\_");
        SendMessage sendMessage = new SendMessage().setChatId(channelName).setText(text.trim());
        sendMessage = sendMessage.enableMarkdown(true).disableWebPagePreview();
        try {
            Message m = execute(sendMessage);
            log.info("Message with board to channel {} sent", channelName);
            return m;
        } catch (TelegramApiException e) {
            log.error("error while send msg to tg_channel " + channelName, e);
        }
        throw new RuntimeException("strange error while send message");
    }

    public void sendMessageToChannel(String channelName, String text, String taskId) {
        Integer messageId = taskToMessageMap.get(taskId);
        if (messageId == null) {
            Message message = sendMessageToChannel(channelName, text);

            if (message.getMessageId() != null) {
                taskToMessageMap.put(taskId, message.getMessageId());
                log.info("new board message in channel, message id:{}", message.getMessageId());
            }

            log.info("Map of [task -> msg_id] = {}", taskToMessageMap);

        } else {
            updateMessageInChannel(channelName, text, messageId);
            log.info("update board message in channel, message id:{}", messageId);
        }
    }

    private void updateMessageInChannel(String channelName, String text, Integer messageId) {
        text = text.replaceAll("_", "\\\\_");
        EditMessageText editMessage = new EditMessageText().setMessageId(messageId)
            .setChatId(channelName).setText(text.trim());
        editMessage = editMessage.enableMarkdown(true).disableWebPagePreview();
        try {
            execute(editMessage);
            log.info("MessageID{} with board to channel {} UPDATED", messageId, channelName);
        } catch (TelegramApiException e) {
            log.error("error while update msg_id=" + messageId + " to tg_channel " + channelName,
                e);
        }
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
