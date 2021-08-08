package ru.sendel.sjctaskschecker.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
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
import ru.sendel.sjctaskschecker.repository.CompetitorRepository;
import ru.sendel.sjctaskschecker.repository.SolutionRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeService {

    private final CompetitorRepository competitorRepository;
    private final SolutionRepository solutionRepository;
    private final TaskService taskService;
    private final TasksPlatform tasksPlatform;

    private final long milliSecondsBetweenUpdateSolution = 60 * 1000;

    @Scheduled(fixedRate = milliSecondsBetweenUpdateSolution)
    public void scheduleRefresh() {
        refreshResultOfTask(taskService.getActualTask());
    }

    public Collection<Solution> refreshResultOfTask(String taskId) {
        return refreshResultOfTask(taskService.getTaskByNumber(taskId));
    }

    public Collection<Solution> refreshResultOfTask(Task task) {
        var taskId = task.getNumber();

        var competitorWithoutSolution = competitorRepository.findAllWithoutSolution(taskId);
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

    public String dashboard() {
        final Task task = taskService.getActualTask();
        final List<Competitor> actualCompetitors = getActiveCompetitors();

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
                .append(format(competitor.durationFromResolveSolution(task)))
                .append("<br>\n");
        }
        return String.join("<br>\n<br>\n", bold(title + titleDeadline),
            taskInfo, (taskStatistic + listOfCompetitors));
    }

    private String bold(String s) {
        return "**" + s + "**";
    }

    private List<Competitor> getActiveCompetitors() {
        return competitorRepository.findAllByIsActiveTrue();
    }

    private String format(Duration d) {
        return d == Duration.ZERO
            ? ""
            : String.format(" - %dч %dмин назад", d.toHours(), d.toMinutesPart());
    }
}

