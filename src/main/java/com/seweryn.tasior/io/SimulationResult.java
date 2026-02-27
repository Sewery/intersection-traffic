package com.seweryn.tasior.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimulationResult {
    private final List<StepStatus> stepStatuses = new ArrayList<>();

    public void addStep(StepStatus status) {
        stepStatuses.add(status);
    }

    public List<StepStatus> getStepStatuses() {
        return Collections.unmodifiableList(stepStatuses);
    }
}
