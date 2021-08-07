package ru.sendel.sjctaskschecker.codewars.api;

import java.util.Collection;
import java.util.Map;
import ru.sendel.sjctaskschecker.model.Competitor;
import ru.sendel.sjctaskschecker.model.Solution;

public interface TasksPlatform {

    Map<Competitor, CompetitorCompletedChallenges> getCompetitorsInfo(
        Collection<Competitor> competitors);

    CompetitorCompletedChallenges getCompetitorInfo(Competitor competitor);
}
