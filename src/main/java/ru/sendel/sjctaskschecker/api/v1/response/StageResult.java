package ru.sendel.sjctaskschecker.api.v1.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StageResult {

    private String taskId;
}
