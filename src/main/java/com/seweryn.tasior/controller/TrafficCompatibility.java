package com.seweryn.tasior.controller;

import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.Movement;
import com.seweryn.tasior.model.Turn;

import java.util.*;

public class TrafficCompatibility {
    private static final List<Set<Movement>> groups = List.of(
            // faza 1: NS prosto + prawo
            Set.of(
                    new Movement(Direction.NORTH, Turn.STRAIGHT),
                    new Movement(Direction.NORTH, Turn.RIGHT),
                    new Movement(Direction.SOUTH, Turn.STRAIGHT),
                    new Movement(Direction.SOUTH, Turn.RIGHT)
            ),
            // faza 2: NS lewo
            Set.of(
                    new Movement(Direction.NORTH, Turn.LEFT),
                    new Movement(Direction.SOUTH, Turn.LEFT)
            ),
            // faza 3: NORTH wszystkie
            Set.of(
                    new Movement(Direction.NORTH, Turn.LEFT),
                    new Movement(Direction.NORTH, Turn.STRAIGHT),
                    new Movement(Direction.NORTH, Turn.RIGHT)
            ),
            // faza 4: SOUTH wszystkie
            Set.of(
                    new Movement(Direction.SOUTH, Turn.LEFT),
                    new Movement(Direction.SOUTH, Turn.STRAIGHT),
                    new Movement(Direction.SOUTH, Turn.RIGHT)
            ),
            // faza 5: EW prosto + prawo
            Set.of(
                    new Movement(Direction.EAST,  Turn.STRAIGHT),
                    new Movement(Direction.EAST,  Turn.RIGHT),
                    new Movement(Direction.WEST,  Turn.STRAIGHT),
                    new Movement(Direction.WEST,  Turn.RIGHT)
            ),
            // faza 6: EW lewo
            Set.of(
                    new Movement(Direction.EAST,  Turn.LEFT),
                    new Movement(Direction.WEST,  Turn.LEFT)
            ),
            // faza 7: EAST wszystkie
            Set.of(
                    new Movement(Direction.EAST,  Turn.LEFT),
                    new Movement(Direction.EAST,  Turn.STRAIGHT),
                    new Movement(Direction.EAST,  Turn.RIGHT)
            ),
            // faza 8: WEST wszystkie
            Set.of(
                    new Movement(Direction.WEST,  Turn.LEFT),
                    new Movement(Direction.WEST,  Turn.STRAIGHT),
                    new Movement(Direction.WEST,  Turn.RIGHT)
            )
    );

    public static List<Set<Movement>> getPhaseGroups() {
        return groups;
    }
}
