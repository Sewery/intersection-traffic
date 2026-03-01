package com.seweryn.tasior.io;

import com.seweryn.tasior.statistics.Statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimulationResult {
    private final List<StepStatus> stepStatuses = new ArrayList<>();
    private Statistics statistics;

    public void addStep(StepStatus status) {
        stepStatuses.add(status);
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public List<StepStatus> getStepStatuses() {
        return Collections.unmodifiableList(stepStatuses);
    }

    public Statistics getStatistics() {
        return statistics;
    }
}
