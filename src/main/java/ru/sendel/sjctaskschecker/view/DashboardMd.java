package ru.sendel.sjctaskschecker.view;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sendel.sjctaskschecker.model.Competitor;
import ru.sendel.sjctaskschecker.model.Solution;
import ru.sendel.sjctaskschecker.model.Task;
import ru.sendel.sjctaskschecker.service.CompetitorService;
import ru.sendel.sjctaskschecker.service.TaskService;

@Component
@RequiredArgsConstructor
@Qualifier("DashboardMd")
public class DashboardMd implements Dashboard {

    private final TaskService taskService;
    private final CompetitorService competitorService;

    @Override
    public String dashboard(Task task) {
        final List<Competitor> actualCompetitors = competitorService.getActiveCompetitors();

        String title = DateTimeFormatter.ofPattern("d MMMM").format(task.getStartActiveTime()) +
            ", Задание №" + task.getNumberInChallenge();

        String titleDeadline = "⏰ Дедлайн - " +
            DateTimeFormatter.ofPattern("d MMMM HH:mm").format(task.getEndActiveTime()) + " МСК";

        StringBuilder taskInfo = new StringBuilder();
        taskInfo.append(
            String.format(bold("%s kyu") + " - %s%nhttps://www.codewars.com/kata/%s",
                task.getDifficult(), task.getName(), task.getNumber()));

        //statistic by users done task
        long amountUsersDoneTask = actualCompetitors.stream()
            .filter(c -> c.getSolutions().stream()
                .anyMatch(s -> s.isSolve(task)))
            .count();

        LocalDateTime lastCheckTask = task.getLastCheckSolutions();
        String taskStatistic = lastCheckTask != null ? "*Выполнили:* "
            + amountUsersDoneTask
            + "/"
            + actualCompetitors.size()
            + " (обновлено:" + DateTimeFormatter.ofPattern("dd.MM HH:mm")
            .format(LocalDateTime.now()) + " МСК)" : "";

        //list of competitors
        actualCompetitors
            .sort(Comparator.comparing(c -> ((Competitor) c).hasSolution(task))
                .reversed()
                .thenComparing(c -> ((Competitor) c).getName().toLowerCase()));

        //fastest solutions
        var fastestCompetitors = actualCompetitors.stream()
            .sorted(Comparator.comparing((Competitor c) ->
                c.durationFromStartToResolveSolution(task)))
            .filter(competitor -> competitor.durationFromStartToResolveSolution(task).compareTo(Duration.ZERO) > 0)
            .limit(3)
            .collect(Collectors.toUnmodifiableList());

        StringBuilder listOfCompetitors = new StringBuilder();
        for (int i = 0; i < actualCompetitors.size(); i++) {
            final Competitor competitor = actualCompetitors.get(i);
            listOfCompetitors.append(String.format("`%2d`", i + 1))
                .append(". ")
                .append(competitor.hasSolution(task) ? "✅" : "❔")
                .append(" ")
                .append(actualCompetitors.get(i).getName())
                .append(fastestCompetitors.contains(competitor) ? " 🤟" : "")
                .append(
                    formatPassedTimeFromTaskSolution(
                        competitor.durationFromStartToResolveSolution(task)))
                .append("\n");
        }

        return String.join("\n\n", bold(title), bold(titleDeadline),
            taskInfo, (taskStatistic + "\n" + listOfCompetitors));
    }

    @Override
    public String formatNewSolutions(Collection<Solution> newSolutions) {
        return "\uD83D\uDC4D *Новые решения прислали:*\n" + newSolutions.stream()
            .filter(solution -> solution.getCompetitor().isActive())
            .map(solution -> solution.getCompetitor().getName())
            .collect(Collectors.joining(", "));
    }

    @Override
    public String dashboard() {
        return dashboard(taskService.getActualTask());
    }

    private String bold(String s) {
        return "*" + s + "*";
    }

    private String formatPassedTimeFromTaskSolution(Duration d) {
        if (d == Duration.ZERO) {
            return "";
        }

        if (d.toHours() < 0) {
            return ", давно решено";
        } else if (d.toHours() < 1) {
            return String.format(" - %dмин ", d.toMinutesPart());
        } else {
            return String.format(" - %dч %dмин", d.toHours(), d.toMinutesPart());
        }
    }
}
