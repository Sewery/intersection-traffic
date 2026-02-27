package com.seweryn.tasior.commands;

import com.seweryn.tasior.model.Direction;

public record AddVehicleCommand(Direction startRoad, Direction endRoad, String vehicleId) implements Command {

    @Override
    public CommandType getType() {
        return CommandType.ADD_VEHICLE;
    }
}
