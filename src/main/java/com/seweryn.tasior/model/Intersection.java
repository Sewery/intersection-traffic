package com.seweryn.tasior.model;

import java.util.*;

public class Intersection {
    private final List<Road> roads;

    public Intersection() {
        this.roads = List.of(
                Road.createThreeLineRoad(Direction.NORTH),
                Road.createThreeLineRoad(Direction.SOUTH),
                Road.createThreeLineRoad(Direction.EAST),
                Road.createThreeLineRoad(Direction.WEST)
        );
    }

    public List<Road> getRoads() { return roads; }

    public Road getRoad(Direction direction) {
        return roads.stream()
                .filter(road -> road.getLocation().equals(direction))
                .findFirst()
                .orElseThrow();
    }
}
