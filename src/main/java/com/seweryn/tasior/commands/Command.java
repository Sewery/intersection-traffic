package com.seweryn.tasior.commands;

public interface Command {
    CommandType getType();

    enum CommandType {
        ADD_VEHICLE,
        STEP,
        CONFIGURE_ALGORITHM,
        UPDATE_LANE_STATUS,
        GET_STATISTICS
    }

    Command STEP = () -> CommandType.STEP;
    Command GET_STATISTICS = () -> CommandType.GET_STATISTICS;
}
