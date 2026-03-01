package com.seweryn.tasior.commands;

import com.seweryn.tasior.controller.TimeSlot;
import com.seweryn.tasior.model.Direction;

import java.util.List;
import java.util.Map;

public record ConfigureAlgorithmCommand(
        Double carPriority,
        Double busPriority,
        AlgorithmMode mode,
        Map<Direction, List<TimeSlot>> historicalData,
        Integer maxWaitTime,
        Integer yellowTime
        ) implements Command {
    @Override
    public CommandType getType() {
        return CommandType.CONFIGURE_ALGORITHM;
    }
}
