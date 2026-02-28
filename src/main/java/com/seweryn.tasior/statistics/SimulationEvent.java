package com.seweryn.tasior.statistics;

import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.Turn;

public sealed interface SimulationEvent permits
        SimulationEvent.VehicleArrived,
        SimulationEvent.VehicleExited,
        SimulationEvent.VehicleStuck,
        SimulationEvent.LaneBlocked,
        SimulationEvent.LaneUnblocked {

    record VehicleArrived(String vehicleId, Direction direction, int step) implements SimulationEvent {}
    record VehicleExited(String vehicleId, int step) implements SimulationEvent {}
    record VehicleStuck(String vehicleId, Direction direction) implements SimulationEvent {}
    record LaneBlocked(Direction road, Turn turn, int vehiclesAffected, int step) implements SimulationEvent {}
    record LaneUnblocked(Direction road, Turn turn, int step) implements SimulationEvent {}
}