package ru.sendel.sjctaskschecker.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sendel.sjctaskschecker.model.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findFirstByNumber(String number);

    //    @Query(value = "From Task t WHERE t.startActiveTime > :date AND t.endActiveTime < :date LIMIT 1")
    Optional<Task> findFirstByStartActiveTimeBeforeAndEndActiveTimeAfter(
        LocalDateTime afterStart,
        LocalDateTime beforeEnd);


}
