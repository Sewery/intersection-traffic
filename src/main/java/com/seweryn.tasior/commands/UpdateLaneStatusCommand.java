package com.seweryn.tasior.commands;

public class UpdateLaneStatusCommand implements Command{
    @Override
    public CommandType getType() {
        return CommandType.UPDATE_LANE_STATUS;
    }
}
