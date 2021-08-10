package ru.sendel.sjctaskschecker.view;

import lombok.RequiredArgsConstructor;
import ru.sendel.sjctaskschecker.service.CompetitorService;
import ru.sendel.sjctaskschecker.service.TaskService;

@RequiredArgsConstructor
public class DashboardHtml implements Dashboard {

    private final TaskService taskService;
    private final CompetitorService competitorService;

    @Override
    public String dashboard() {
        return null;
    }
}
