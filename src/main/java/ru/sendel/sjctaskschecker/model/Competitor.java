package ru.sendel.sjctaskschecker.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "competitors")
@Data
@NoArgsConstructor
@ToString(exclude = "solutions")
public class Competitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    private boolean isActive = true;

    @NotNull
    @NotEmpty
    private String telegramId;

    @NotNull
    @NotEmpty
    private String codewarsName;

    @OneToMany(mappedBy = "competitor", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Solution> solutions = new ArrayList<>();

    public boolean hasSolution(Task task) {
      return   solutions.stream()
            .anyMatch(s -> s.isSolve(task));
    }

    public Duration durationFromStartToResolveSolution(Task task) {
        return solutions.stream()
            .filter(s-> s.isSolve(task))
            .map(s-> Duration.between(task.getStartActiveTime(), s.getSolutionSubmitTime()))
            .findFirst().orElse(Duration.ZERO);
    }
}
