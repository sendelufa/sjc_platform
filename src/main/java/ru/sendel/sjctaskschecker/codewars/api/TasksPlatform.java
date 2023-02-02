package ru.sendel.sjctaskschecker.codewars.api;

import java.util.Collection;
import java.util.Map;
import ru.sendel.sjctaskschecker.model.Competitor;

public interface TasksPlatform {

    Map<Competitor, CompetitorCompletedChallenges> getCompetitorsInfo(
        Collection<Competitor> competitors);

    CompetitorCompletedChallenges getCompetitorInfo(Competitor competitor);
}
