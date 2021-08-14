package ru.sendel.sjctaskschecker.service;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.sendel.sjctaskschecker.model.Solution;
import ru.sendel.sjctaskschecker.model.Task;
import ru.sendel.sjctaskschecker.telegram.TelegramBot;
import ru.sendel.sjctaskschecker.view.Dashboard;

@Service
@Slf4j
public class ChallengeService {

    private final TaskService taskService;
    private final SolutionService solutionService;
    private final TelegramBot telegramBot;

    private final Dashboard dashboard;
    @Value("${bot.channel}")
    private String telegramChannelNameToPublish;

    private final long milliSecondsBetweenUpdateSolution = 30 * 60 * 1000;

    public ChallengeService(
        TaskService taskService,
        SolutionService solutionService,
        TelegramBot telegramBot,
        @Qualifier(value = "DashboardMd")
            Dashboard dashboard) {
        this.taskService = taskService;
        this.solutionService = solutionService;
        this.telegramBot = telegramBot;
        this.dashboard = dashboard;
    }

    @Scheduled(fixedRate = milliSecondsBetweenUpdateSolution)
    public void scheduleRefresh() {
        Task actualTask = taskService.getActualTask();
        Collection<Solution> newSolutions = solutionService.refreshResultOfTask(actualTask);
        telegramBot.sendMessageToChannel(telegramChannelNameToPublish, dashboard.dashboard(),
            actualTask.getNumber());

        if (!newSolutions.isEmpty()) {
            telegramBot.sendMessageToChannel(telegramChannelNameToPublish,
                dashboard.formatNewSolutions(newSolutions));
        }
    }

    public String dashboard() {
        return dashboard.dashboard();
    }
}

