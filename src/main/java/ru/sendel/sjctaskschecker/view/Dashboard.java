package ru.sendel.sjctaskschecker.view;

import java.util.Collection;
import ru.sendel.sjctaskschecker.model.Solution;
import ru.sendel.sjctaskschecker.model.Task;

public interface Dashboard {
    String dashboard();
    String dashboard(Task task);
    String formatNewSolutions(Collection<Solution> newSolutions);

}
