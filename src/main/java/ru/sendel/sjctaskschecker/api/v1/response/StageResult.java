package ru.sendel.sjctaskschecker.api.v1.response;

import jdk.jfr.DataAmount;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class StageResult {

    private String taskId;
}
