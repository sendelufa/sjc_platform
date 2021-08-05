package ru.sendel.sjctaskschecker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sendel.sjctaskschecker.model.Solution;

public interface SolutionRepository extends JpaRepository<Solution, Long> {

}
