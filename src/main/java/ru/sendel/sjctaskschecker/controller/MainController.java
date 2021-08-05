package ru.sendel.sjctaskschecker.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sendel.sjctaskschecker.api.v1.response.StageResult;
import ru.sendel.sjctaskschecker.repository.CompetitorRepository;
import ru.sendel.sjctaskschecker.service.ChallengeService;

@RestController
@RequestMapping("/api/v1/")
@AllArgsConstructor
public class MainController {

    private final CompetitorRepository competitorRepository;
    private final ChallengeService challengeService;

    @GetMapping("result/{taskId}")
    public StageResult stageResult(@PathVariable String taskId) {
        return challengeService.refreshResultOfTask(taskId);
    }

    @GetMapping("/td")
    public void addTestData(){
        challengeService.addTestData();
    }

    @GetMapping("/dashboard")
    public String dashboard(){
        return challengeService.dashboard();
    }
}
