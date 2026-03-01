package com.seweryn.tasior.commands;

import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.VehicleType;

public record AddVehicleCommand(Direction startRoad, Direction endRoad, String vehicleId, VehicleType vehicleType) implements Command {

    @Override
    public CommandType getType() {
        return CommandType.ADD_VEHICLE;
    }
}
