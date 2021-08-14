package ru.sendel.sjctaskschecker.controller;

import java.util.Collection;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sendel.sjctaskschecker.model.Task;
import ru.sendel.sjctaskschecker.repository.CompetitorRepository;
import ru.sendel.sjctaskschecker.service.ChallengeService;
import ru.sendel.sjctaskschecker.service.SolutionService;
import ru.sendel.sjctaskschecker.service.TaskService;

@RestController
@RequestMapping("/api/v1/")
@AllArgsConstructor
public class MainController {

    private final CompetitorRepository competitorRepository;
    private final ChallengeService challengeService;
    private final TaskService taskService;
    private final SolutionService solutionService;

    @GetMapping("result/{taskId}")
    public Collection<?> stageResult(@PathVariable String taskId) {
        return solutionService.refreshResultOfTask(taskId);
    }

    @GetMapping("/td")
    public Task addTestData(){
        return taskService.getActualTask();
    }

    @GetMapping("/dashboard")
    public String dashboard(){
        return challengeService.dashboard();
    }
}
