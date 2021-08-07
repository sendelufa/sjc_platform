package ru.sendel.sjctaskschecker.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sendel.sjctaskschecker.model.Competitor;

@Repository
public interface CompetitorRepository extends JpaRepository<Competitor, Long> {

//    @Query("From Competitor c WHERE 0 = (select count(s.id) from Solution s WHERE s.competitor = c AND s.taskId = :taskId)")
    @Query(value = "SELECT * FROM competitors as c"
        + " WHERE"
        + " (SELECT COUNT(*) FROM solutions as s "
        + "  WHERE s.competitor_id = c.id AND s.task_id = :taskId AND s.is_done = true) "
        + " = 0",
        nativeQuery = true)
    List<Competitor> findAllWithoutSolution(@Param("taskId") String taskId);

    List<Competitor> findAllByIsActiveTrue();


}
