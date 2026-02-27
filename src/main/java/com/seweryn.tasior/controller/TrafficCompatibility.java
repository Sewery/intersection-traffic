package com.seweryn.tasior.controller;

import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.Movement;
import com.seweryn.tasior.model.Turn;

import java.util.*;

public class TrafficCompatibility {
    private static final List<Set<Movement>> groups;
    static {
        groups = getCompatibleGroups();
    }
    public static List<Set<Movement>> getPhaseGroups() {
        return groups;
    }
    private static List<Set<Movement>> getCompatibleGroups() {
        List<Set<Movement>> groups = new ArrayList<>();

        groups.add(Set.of(
                new Movement(Direction.NORTH, Turn.STRAIGHT),
                new Movement(Direction.NORTH, Turn.RIGHT),
                new Movement(Direction.SOUTH, Turn.STRAIGHT),
                new Movement(Direction.SOUTH, Turn.RIGHT)
        ));

        groups.add(Set.of(
                new Movement(Direction.NORTH, Turn.LEFT),
                new Movement(Direction.SOUTH, Turn.LEFT)
        ));

        groups.add(Set.of(
                new Movement(Direction.NORTH, Turn.LEFT),
                new Movement(Direction.NORTH, Turn.STRAIGHT),
                new Movement(Direction.NORTH, Turn.RIGHT)
        ));

        groups.add(Set.of(
                new Movement(Direction.SOUTH, Turn.LEFT),
                new Movement(Direction.SOUTH, Turn.STRAIGHT),
                new Movement(Direction.SOUTH, Turn.RIGHT)
        ));
        //
        groups.add(Set.of(
                new Movement(Direction.WEST, Turn.STRAIGHT),
                new Movement(Direction.WEST, Turn.RIGHT),
                new Movement(Direction.EAST, Turn.STRAIGHT),
                new Movement(Direction.EAST, Turn.RIGHT)
        ));

        groups.add(Set.of(
                new Movement(Direction.WEST, Turn.LEFT),
                new Movement(Direction.EAST, Turn.LEFT)
        ));

        groups.add(Set.of(
                new Movement(Direction.WEST, Turn.LEFT),
                new Movement(Direction.WEST, Turn.STRAIGHT),
                new Movement(Direction.WEST, Turn.RIGHT)
        ));

        groups.add(Set.of(
                new Movement(Direction.EAST, Turn.LEFT),
                new Movement(Direction.EAST, Turn.STRAIGHT),
                new Movement(Direction.EAST, Turn.RIGHT)
        ));

        return groups;
    }
}
