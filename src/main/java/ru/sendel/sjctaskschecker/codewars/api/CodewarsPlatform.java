package ru.sendel.sjctaskschecker.codewars.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.sendel.sjctaskschecker.model.Competitor;

@Component
@Slf4j
public class CodewarsPlatform implements TasksPlatform {

    @Value("${codewars.user}")
    private String userApi;

    @Value("${codewars.user.completed}")
    private String completedApi;

    @Override
    public Map<Competitor, CompetitorCompletedChallenges> getCompetitorsInfo(
        Collection<Competitor> competitors) {
        Map<Competitor, CompetitorCompletedChallenges> completedChallengesMap = new HashMap<>();

        for (var competitor : competitors) {
            completedChallengesMap.put(competitor, getCompetitorInfo(competitor));
        }

        return completedChallengesMap;
    }

    @Override
    public CompetitorCompletedChallenges getCompetitorInfo(Competitor competitor) {
        log.info("⌜---- Start request info for codewars user {}", competitor.getCodewarsName());
        var completedChallenges =
            getFromRestRequest(userApiInfoUrl(competitor));

        for (int pageNumber = 1; pageNumber < completedChallenges.getTotalPages(); pageNumber++) {
            var nextPage = getFromRestRequest(userApiInfoUrl(competitor, pageNumber));
            completedChallenges.addToData(nextPage);
        }
        log.info("⌞---- End request user {}, get {} solutions", competitor.getCodewarsName(), completedChallenges.getData().size());
        return completedChallenges;

    }

    private CompetitorCompletedChallenges getFromRestRequest(String apiUrl) {
        try {
            return new RestTemplate().getForObject(apiUrl, CompetitorCompletedChallenges.class);
        } catch (HttpClientErrorException httpClientErrorException){
            return CompetitorCompletedChallenges.safeFakeObject();
        }
    }

    private String userApiInfoUrl(Competitor competitor) {
        return userApi + competitor.getCodewarsName() + completedApi;
    }

    private String userApiInfoUrl(Competitor competitor, int pageNumber) {
        return userApiInfoUrl(competitor) + "?page=" + pageNumber;
    }
}
