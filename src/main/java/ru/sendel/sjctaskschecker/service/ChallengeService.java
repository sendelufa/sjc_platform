package ru.sendel.sjctaskschecker.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.sendel.sjctaskschecker.api.v1.response.StageResult;
import ru.sendel.sjctaskschecker.codewars.CompetitorCompletedChallenges;
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

    @Value("${codewars.user}")
    private String userApi;

    @Value("${codewars.user.completed}")
    private String completedApi;
    private final long milliSecondsBetweenUpdateSolution = 1 * 60 * 1000;

    public StageResult refreshResultOfTask(String taskId) {
        if (!taskService.getAllTasksId().contains(taskId)) {
            log.error("(обновление данных о задачу) Задача {} не найдена!", taskId);
            throw new NoSuchElementException("Задача не найдена");
        }

        List<Competitor> competitorWithoutSolution = competitorRepository
            .findAllWithoutSolution(taskId);
        log.info("Задачу #{} не выполнили {} участников", taskId, competitorWithoutSolution.size());

        Map<Competitor, CompetitorCompletedChallenges> competitorCompletedChallenges =
            getCompetitorsInfo(competitorWithoutSolution);

        List<Solution> newSolutions = competitorCompletedChallenges.entrySet()
            .stream()
            .filter(entry -> entry.getValue().isCompleteChallenge(taskId))
            .collect(Collectors.toMap(Entry::getKey,
                entry -> entry.getValue().getCompletedChallenge(taskId)))
            .entrySet().stream()
            .map(entry -> (Solution.build(entry.getKey(), entry.getValue().get())))
            .collect(Collectors.toList());

        log.info("Обновление решений пользователей {}", newSolutions);

        solutionRepository.saveAll(newSolutions);

        log.info("Обновление закончено");

        return StageResult.builder()
            .taskId(newSolutions.toString())
            .build();
    }

    @Scheduled(fixedRate = milliSecondsBetweenUpdateSolution)
    public void scheduleRefresh(){
        refreshResultOfTask("5980de1a17d1fee3db000059");
    }

    public String dashboard() {
        String taskNumber = "5980de1a17d1fee3db000059";
        Task task = taskService.getTaskByNumber(taskNumber);
        List<Competitor> actualCompetitors = competitorRepository.findAllByIsActive(true);

        StringBuilder dashboard = new StringBuilder();
        dashboard.append(
            String.format("**%s kyu** - %s<br/ >%nhttps://www.codewars.com/kata/%s<br/ >\n<br/ >\n",
                task.getDifficult(), task.getName(), task.getNumber()));

        //statistic by users done task
        long amountUsersDoneTask = actualCompetitors.stream()
            .filter(c -> c.getSolutions().stream().anyMatch(s -> s.getTaskId().equals(taskNumber)))
            .count();

        StringBuilder taskStatistic = new StringBuilder()
            .append("Выполнили: ")
            .append(amountUsersDoneTask)
            .append("/")
            .append(actualCompetitors.size())
            .append("<br>\n");


        //list of competitors
        actualCompetitors.sort(Comparator.comparing(Competitor::getName));
        StringBuilder listOfCompetitors = new StringBuilder();
        for (int i = 0; i < actualCompetitors.size(); i++) {
            listOfCompetitors.append(i + 1)
                .append(". ")
                .append(actualCompetitors.get(i)
                    .getSolutions()
                    .stream()
                    .anyMatch(s -> s.getTaskId().equals(taskNumber)) ? "✅" : "❔")
                .append(" @")
                .append(actualCompetitors.get(i).getName())
                .append("<br>\n");
        }
        return dashboard
            .append(taskStatistic)
            .append(listOfCompetitors).toString();
    }


    private Map<Competitor, CompetitorCompletedChallenges> getCompetitorsInfo(
        Collection<Competitor> competitors) {
        Map<Competitor, CompetitorCompletedChallenges> completedChallengesMap = new HashMap<>();

        for (var competitor : competitors) {
            completedChallengesMap.put(competitor, getCompetitorInfo(competitor));
        }

        return completedChallengesMap;
    }

    private CompetitorCompletedChallenges getCompetitorInfo(Competitor competitor) {
        RestTemplate restTemplate = new RestTemplate();
        var completedChallenges = restTemplate
            .getForObject(userApiInfoUrl(competitor), CompetitorCompletedChallenges.class);

        for (int pageNumber = 1; pageNumber < completedChallenges.getTotalPages(); pageNumber++) {
            var nextPage = restTemplate.getForObject(userApiInfoUrlWithPage(competitor, pageNumber),
                CompetitorCompletedChallenges.class);
            completedChallenges.addToData(nextPage);
        }
        return completedChallenges;
    }

    private String userApiInfoUrl(Competitor competitor) {
        return userApi + competitor.getCodewarsName() + completedApi;
    }

    private String userApiInfoUrlWithPage(Competitor competitor, int pageNumber) {
        return userApi + competitor.getCodewarsName() + completedApi + "?page=" + pageNumber;
    }

    @Transactional
    public void addTestData() {

        Competitor competitor = new Competitor();
        competitor.setTelegramId("1111");
        competitor.setCodewarsName("sendelufa");
        competitor.setName("sendel");

        competitorRepository.save(competitor);

        Competitor competitor2 = new Competitor();
        competitor2.setTelegramId("2222");
        competitor2.setCodewarsName("KofeNata");
        competitor2.setName("Natalia");

        competitorRepository.save(competitor2);

        Solution solution = new Solution();
        solution.setSolutionSubmitTime(LocalDateTime.now());
        solution.setTaskId("5583090cbe83f4fd8c000051");
        solution.setCompetitor(competitor);
        solution.setLastCheckSolution(LocalDateTime.now());
        solution.setDone(true);

        solutionRepository.save(solution);
    }
}
