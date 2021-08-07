package ru.sendel.sjctaskschecker.model;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "tasks")
@Data
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numberInChallenge;

    private boolean isActive;

    @NotNull
    private String number;

    @NotNull
    private String name;

    @NotNull
    private String difficult;

    @NotNull
    private LocalDateTime startActiveTime;

    @NotNull
    private LocalDateTime endActiveTime;

    private LocalDateTime lastCheckSolutions;

}
