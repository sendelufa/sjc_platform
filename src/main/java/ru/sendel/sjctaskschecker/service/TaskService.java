package ru.sendel.sjctaskschecker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sendel.sjctaskschecker.model.Task;
import ru.sendel.sjctaskschecker.repository.TaskRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskByNumber(String number) {
        return taskRepository.findFirstByNumber(number).orElseThrow(() -> {
            log.error("(обновление данных о задачу) Задача {} не найдена!", number);
            throw new NoSuchElementException("Задача не найдена");
        });
    }

    public Task getActualTask() {
        return taskRepository.findFirstByStartActiveTimeBeforeAndEndActiveTimeAfter(
                LocalDateTime.now(), LocalDateTime.now())
            .orElseThrow();
    }

    public void updateLastCheckStatus(Task task) {
        task.setLastCheckSolutions(LocalDateTime.now());
        taskRepository.save(task);
    }
}
