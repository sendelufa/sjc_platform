package ru.sendel.sjctaskschecker.codewars;

import java.util.List;
import java.util.Optional;
import lombok.Data;

@Data
public class CompetitorCompletedChallenges {

    private int totalPages;
    private int totalItems;
    private List<Challenge> data;

    public void addToData(CompetitorCompletedChallenges nextPage) {
        data.addAll(nextPage.data);
    }

    public boolean isCompleteChallenge(String challengeId) {
        return data.stream()
            .anyMatch(c -> c.getId().equals(challengeId));
    }

    public Optional<Challenge> getCompletedChallenge(String challengeId) {
        return data.stream()
            .filter(c -> c.getId().equals(challengeId))
            .findFirst();
    }

    @Data
    public static class Challenge {

        private String id;
        private String name;
        private String slug;
        private String completedAt;
        private List<String> completedLanguages;
    }
}
