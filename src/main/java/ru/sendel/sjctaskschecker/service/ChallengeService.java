package ru.sendel.sjctaskschecker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.sendel.sjctaskschecker.telegram.TelegramBot;
import ru.sendel.sjctaskschecker.view.Dashboard;

@Service
@Slf4j
public class ChallengeService {

    private final TaskService taskService;
    private final SolutionService solutionService;
    private final TelegramBot telegramBot;

    private final Dashboard dashboard;

    private final long milliSecondsBetweenUpdateSolution = 5 * 60 * 1000;

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
        solutionService.refreshResultOfTask(taskService.getActualTask());
        telegramBot.sendMessageToChannel("@cjs_test", dashboard.dashboard());
    }

    public String dashboard() {
        return dashboard.dashboard();
    }
}

