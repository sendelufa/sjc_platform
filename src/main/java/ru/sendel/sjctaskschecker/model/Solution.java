package ru.sendel.sjctaskschecker.model;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.sendel.sjctaskschecker.codewars.CompetitorCompletedChallenges.Challenge;

@Entity(name = "solutions")
@Data
@NoArgsConstructor
public class Solution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Competitor competitor;

    @NotNull
    @NotEmpty
    private String taskId;

    private boolean isDone;

    private LocalDateTime solutionSubmitTime;

    private LocalDateTime lastCheckSolution;

    public static Solution build(Competitor competitor, Challenge challenge) {
        Solution solution = new Solution();
        solution.setDone(true);
        solution.setCompetitor(competitor);
        solution.setLastCheckSolution(LocalDateTime.now());
        // TODO: Date Parse pattern for 2017-04-06T16:32:09Z
        // remove Z
        solution.setSolutionSubmitTime(LocalDateTime.parse(
            challenge.getCompletedAt().substring(0, challenge.getCompletedAt().length() - 1)));
        solution.setTaskId(challenge.getId());
        return solution;
    }
}
