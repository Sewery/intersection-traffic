package com.seweryn.tasior.model;

import java.util.*;

public class Road {
    private final Direction location;
    private final List<Lane> lanes;
    private final Map<Turn, Lane> lanesPerTurn;

    public Road(Direction location, List<Lane> lanes) {
        this.location = location;
        this.lanes = lanes;
        this.lanesPerTurn = new HashMap<>();
        lanes.forEach(lane -> lanesPerTurn.put(lane.getAllowedTurn(), lane));
    }

    public static Road createThreeLineRoad(Direction direction) {
        return new Road(direction, List.of(
                new Lane(Turn.STRAIGHT),
                new Lane(Turn.LEFT),
                new Lane(Turn.RIGHT)
        ));
    }

    public void addVehicleToLane(Direction endRoad, Vehicle vehicle) {
        Turn vehicleTurn = Direction.findTurn(location, endRoad);
        Lane vehicleLane = lanesPerTurn.get(vehicleTurn);
        if (vehicleLane != null) {
            vehicleLane.addVehicle(vehicle);
        }
    }

    public Lane getLaneByTurn(Turn turn) {
        return lanesPerTurn.get(turn);
    }

    public Collection<Lane> getLanes() {
        return Collections.unmodifiableList(lanes);
    }

    public Direction getLocation() {
        return location;
    }

}
