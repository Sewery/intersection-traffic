package com.seweryn.tasior.commands;

import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.Turn;

public record UpdateLaneStatusCommand(
        Direction road,
        Turn turn,
        boolean blocked
) implements Command {
    @Override
    public CommandType getType() {
        return CommandType.UPDATE_LANE_STATUS;
    }
}
