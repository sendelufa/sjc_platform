package ru.sendel.sjctaskschecker.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sendel.sjctaskschecker.model.Competitor;
import ru.sendel.sjctaskschecker.repository.CompetitorRepository;

@Service
@RequiredArgsConstructor
public class CompetitorService {
    private final CompetitorRepository competitorRepository;
    public List<Competitor> getActiveCompetitors() {
        return competitorRepository.findAllByIsActiveTrue();
    }

    public List<Competitor> getWithoutSolutions(String taskId){
        return competitorRepository.findAllWithoutSolution(taskId);
    }
}
