package ru.sendel.sjctaskschecker.telegram;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
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
import ru.sendel.sjctaskschecker.model.Solution;
import ru.sendel.sjctaskschecker.model.Task;
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

    private final TaskService taskService;
    private final SolutionService solutionService;

    private final List<Long> authorizeChatId = List.of(302722922L, 1039061325L, 349347653L, 1181136L);

    private final Dashboard dashboard;

    Map<String, Integer> taskToMessageMap = new ConcurrentHashMap<>();

    public TelegramBot(TaskService taskService,
        SolutionService solutionService,
        @Qualifier("DashboardMd") Dashboard dashboard) {
        this.taskService = taskService;
        this.solutionService = solutionService;
        this.dashboard = dashboard;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || update.getMessage().getText() == null) {
            return;
        }

        Long chatIdUpdate = update.getMessage().getChatId();
        if (!authorizeChatId.contains(chatIdUpdate)){
            return;
        }

        String messageText = update.getMessage().getText();
        log.info("new msg:{} from {} {}", messageText, chatIdUpdate,
            update.getMessage().getChat().getUserName());

        SendMessage sendMessage;

        if (messageText.equals("/board")) {
            log.info("telegram_msg: board with actual task");
            try {
                sendMessage = new SendMessage().enableMarkdown(true)
                    .setText(dashboard.dashboard().replaceAll("_", "\\\\_").trim())
                    .setChatId(chatIdUpdate)
                    .disableWebPagePreview();
                execute(sendMessage);
            } catch (NoSuchElementException e) {
                log.error("Actual task not found");
                sendMessage = new SendMessage()
                    .setChatId(chatIdUpdate)
                    .setText("?????????????????????? ?????????????? ??????, ???????????? ???????????????????????? `/tasks`"
                        + " ?????? ?????????????????? ???????????? ???????????? ?? `/board_????????????????????????` "
                        + "- ?????? ?????????????????? ????????????????????")
                    .enableMarkdown(true);
                execute(sendMessage);
            }
        } else if (messageText.matches("/board_.+")) {
            log.info("telegram_msg: board with specific id({})", messageText);

            String taskId = messageText.split("_", 2)[1];
            try {
                Collection<Solution> newSolutions = solutionService.refreshResultOfTask(taskId);
                sendMessage = new SendMessage().enableMarkdown(true)
                    .setText(dashboard.dashboard(taskService.getTaskByNumber(taskId))
                        .replaceAll("_", "\\\\_").trim())
                    .setChatId(chatIdUpdate)
                    .disableWebPagePreview();
                execute(sendMessage);
                if (!newSolutions.isEmpty()) {
                    execute(new SendMessage().enableMarkdown(true)
                        .setText(dashboard.formatNewSolutions(newSolutions)
                            .replaceAll("_", "\\\\_").trim())
                        .setChatId(chatIdUpdate)
                        .disableWebPagePreview());
                }

            } catch (NoSuchElementException e) {
                log.error(e);
                sendMessage = new SendMessage().setText("?????????????? ???? ??????????????")
                    .setChatId(chatIdUpdate);
                execute(sendMessage);

            }
        } else if (messageText.equals("/tasks")) {
            sendMessage = new SendMessage().setChatId(chatIdUpdate)
                .setText(taskService.getAllTasks().stream()
                    .map(Task::toString)
                    .collect(Collectors.joining("\n\n")))
                .enableMarkdown(true)
                .disableWebPagePreview();
            execute(sendMessage);
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

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
