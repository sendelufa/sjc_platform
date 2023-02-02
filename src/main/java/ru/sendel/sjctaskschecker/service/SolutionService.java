package ru.sendel.sjctaskschecker.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sendel.sjctaskschecker.codewars.api.CompetitorCompletedChallenges;
import ru.sendel.sjctaskschecker.codewars.api.TasksPlatform;
import ru.sendel.sjctaskschecker.model.Competitor;
import ru.sendel.sjctaskschecker.model.Solution;
import ru.sendel.sjctaskschecker.model.Task;
import ru.sendel.sjctaskschecker.repository.SolutionRepository;

@Service
@Slf4j
public class SolutionService {

    private final SolutionRepository solutionRepository;
    private final CompetitorService competitorService;
    private final TasksPlatform tasksPlatform;
    private final TaskService taskService;

    public SolutionService(
        SolutionRepository solutionRepository,
        CompetitorService competitorService,
        TasksPlatform tasksPlatform, TaskService taskService) {
        this.solutionRepository = solutionRepository;
        this.competitorService = competitorService;
        this.tasksPlatform = tasksPlatform;
        this.taskService = taskService;
    }

    public Collection<Solution> refreshResultOfTask(String taskId) {
        return refreshResultOfTask(taskService.getTaskByNumber(taskId));
    }

    @Transactional
    public Collection<Solution> refreshResultOfTask(Task task) {
        var taskId = task.getNumber();

        var competitorWithoutSolution = competitorService.getWithoutSolutions(taskId);
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
}
