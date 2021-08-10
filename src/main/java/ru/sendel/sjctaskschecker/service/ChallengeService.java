package ru.sendel.sjctaskschecker.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.sendel.sjctaskschecker.codewars.api.CompetitorCompletedChallenges;
import ru.sendel.sjctaskschecker.codewars.api.TasksPlatform;
import ru.sendel.sjctaskschecker.model.Competitor;
import ru.sendel.sjctaskschecker.model.Solution;
import ru.sendel.sjctaskschecker.model.Task;
import ru.sendel.sjctaskschecker.repository.SolutionRepository;
import ru.sendel.sjctaskschecker.telegram.TelegramBot;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeService {

    private final SolutionRepository solutionRepository;
    private final TaskService taskService;
    private final CompetitorService competitorService;
    private final TasksPlatform tasksPlatform;
    private final TelegramBot telegramBot;

    private final long milliSecondsBetweenUpdateSolution = 5 * 60 * 1000;

    @Scheduled(fixedRate = milliSecondsBetweenUpdateSolution)
    public void scheduleRefresh() {
        refreshResultOfTask(taskService.getActualTask());
        telegramBot.sendMessageToChannel("@cjs_test", dashboardMD2());
    }


    public Collection<Solution> refreshResultOfTask(String taskId) {
        return refreshResultOfTask(taskService.getTaskByNumber(taskId));
    }

    public Collection<Solution> refreshResultOfTask(Task task) {
        var taskId = task.getNumber();

        var competitorWithoutSolution =competitorService.getWithoutSolutions(taskId);
        log.info("Задачу #{} не выполнили {} участников", taskId, competitorWithoutSolution.size());

        Map<Competitor, CompetitorCompletedChallenges> competitorsAllChallengesData =
            tasksPlatform.getCompetitorsInfo(competitorWithoutSolution);

        List<Solution> newSolutions =
            competitorsAllChallengesData.entrySet().stream()
                .filter(entry -> entry.getValue().isCompleteChallenge(taskId))
                .map(entry -> (Solution.build(entry.getKey(),
                    entry.getValue().getCompletedChallenge(taskId).get())))
                .collect(Collectors.toList());

        log.info("Обновление решений пользователей {}", newSolutions);

        List<Solution> solutions = solutionRepository.saveAll(newSolutions);
        taskService.updateLastCheckStatus(task);

        log.info("Обновление закончено");

        return solutions;
    }

    public String dashboardMD2() {
        final Task task = taskService.getActualTask();
        final List<Competitor> actualCompetitors = competitorService.getActiveCompetitors();

        String title = DateTimeFormatter.ofPattern("d MMMM").format(task.getStartActiveTime()) +
            ", Задание №" + task.getNumberInChallenge();

        String titleDeadline = "⏰ Дедлайн - " +
            DateTimeFormatter.ofPattern("d MMMM HH:mm").format(task.getEndActiveTime()) + " МСК";

        StringBuilder taskInfo = new StringBuilder();
        taskInfo.append(
            String.format(bold("%s kyu") + " - %s\n\n%nhttps://www.codewars.com/kata/%s",
                task.getDifficult(), task.getName(), task.getNumber()));

        //statistic by users done task
        long amountUsersDoneTask = actualCompetitors.stream()
            .filter(c -> c.getSolutions().stream()
                .anyMatch(s -> s.isSolve(task)))
            .count();

        LocalDateTime lastCheckTask = task.getLastCheckSolutions();
        String taskStatistic = lastCheckTask != null ? "Выполнили: "
            + amountUsersDoneTask
            + "/"
            + actualCompetitors.size()
            + " (обновлено:" + DateTimeFormatter.ofPattern("dd.MM HH:mm")
            .format(LocalDateTime.now()) + " МСК) \n\n" : "";

        //list of competitors
        actualCompetitors.sort(Comparator.comparing(c -> ((Competitor) c).hasSolution(task))
            .reversed()
            .thenComparing(c -> ((Competitor) c).getName()));

        StringBuilder listOfCompetitors = new StringBuilder();
        for (int i = 0; i < actualCompetitors.size(); i++) {
            final Competitor competitor = actualCompetitors.get(i);
            listOfCompetitors.append(String.format("%02d", i + 1))
                .append(". ")
                .append(competitor.hasSolution(task) ? "✅" : "❔")
                .append(" ")
                .append(actualCompetitors.get(i).getName())
                .append(
                    formatPassedTimeFromTaskSolution(competitor.durationFromResolveSolution(task)))
                .append("\n");
        }
        return String.join("\n\n", bold(title),bold(titleDeadline),
            taskInfo, (taskStatistic + listOfCompetitors));
    }


    public String dashboard() {
        final Task task = taskService.getActualTask();
        final List<Competitor> actualCompetitors = competitorService.getActiveCompetitors();

        String title = DateTimeFormatter.ofPattern("d MMMM").format(task.getStartActiveTime()) +
            ", Задание №" + task.getNumberInChallenge() + "<br>\n";

        String titleDeadline = "⏰ Дедлайн - " +
            DateTimeFormatter.ofPattern("d MMMM HH:mm").format(task.getEndActiveTime()) + " МСК";

        StringBuilder taskInfo = new StringBuilder();
        taskInfo.append(
            String.format(bold("%s kyu") + " - %s<br/ >%nhttps://www.codewars.com/kata/%s",
                task.getDifficult(), task.getName(), task.getNumber()));

        //statistic by users done task
        long amountUsersDoneTask = actualCompetitors.stream()
            .filter(c -> c.getSolutions().stream()
                .anyMatch(s -> s.isSolve(task)))
            .count();

        LocalDateTime lastCheckTask = task.getLastCheckSolutions();
        String taskStatistic = lastCheckTask != null ? "Выполнили: "
            + amountUsersDoneTask
            + "/"
            + actualCompetitors.size()
            + " (обновлено:" + DateTimeFormatter.ofPattern("dd.MM HH:mm")
            .format(LocalDateTime.now()) + " МСК) <br>\n" : "";

        //list of competitors
        actualCompetitors.sort(Comparator.comparing(c -> ((Competitor) c).hasSolution(task))
            .reversed()
            .thenComparing(c -> ((Competitor) c).getName()));

        StringBuilder listOfCompetitors = new StringBuilder();
        for (int i = 0; i < actualCompetitors.size(); i++) {
            final Competitor competitor = actualCompetitors.get(i);
            listOfCompetitors.append(String.format("%02d", i + 1))
                .append(". ")
                .append(competitor.hasSolution(task) ? "✅" : "❔")
                .append(" @")
                .append(actualCompetitors.get(i).getName())
                .append(
                    formatPassedTimeFromTaskSolution(competitor.durationFromResolveSolution(task)))
                .append("<br>\n");
        }
        return String.join("<br>\n<br>\n", bold(title + titleDeadline),
            taskInfo, (taskStatistic + listOfCompetitors));
    }

    private String bold(String s) {
        return "*" + s + "*";
    }



    private String formatPassedTimeFromTaskSolution(Duration d) {
        if (d == Duration.ZERO) {
            return "";
        }

        return d.toHours() > 24 ? ", давно решено"
            : String.format(" - %dч %dмин назад", d.toHours(), d.toMinutesPart());
    }
}

