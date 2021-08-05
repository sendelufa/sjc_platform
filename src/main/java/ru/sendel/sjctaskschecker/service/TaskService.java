package ru.sendel.sjctaskschecker.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sendel.sjctaskschecker.model.Task;
import ru.sendel.sjctaskschecker.repository.TaskRepository;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private List<Task> tasks;

    public List<Task> getAllTasks() {
        if (tasks == null) {
            tasks = taskRepository.findAll();
        }
        return tasks;
    }

    public List<String> getAllTasksId() {
        return getAllTasks().stream()
            .map(Task::getNumber)
            .collect(Collectors.toList());
    }

    public Task getTaskByNumber(String number) {
        return taskRepository.findFirstByNumber(number).orElseThrow();
    }

}
