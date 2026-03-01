package com.seweryn.tasior.controller;

import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.Movement;
import com.seweryn.tasior.model.Turn;

import java.util.*;
/**
 * Defines compatible traffic movement phases for a four-way intersection.
 * Each phase is a set of movements that can be active simultaneously
 * without causing conflicts between vehicles.
 */
public class TrafficCompatibility {
    private static final List<Set<Movement>> groups = List.of(
            // Phase 1: North-South straight and right turn
            Set.of(
                    new Movement(Direction.NORTH, Turn.STRAIGHT),
                    new Movement(Direction.NORTH, Turn.RIGHT),
                    new Movement(Direction.SOUTH, Turn.STRAIGHT),
                    new Movement(Direction.SOUTH, Turn.RIGHT)
            ),
            // Phase 2: North-South left turn
            Set.of(
                    new Movement(Direction.NORTH, Turn.LEFT),
                    new Movement(Direction.SOUTH, Turn.LEFT)
            ),
            // Phase 3: North – all movements
            Set.of(
                    new Movement(Direction.NORTH, Turn.LEFT),
                    new Movement(Direction.NORTH, Turn.STRAIGHT),
                    new Movement(Direction.NORTH, Turn.RIGHT)
            ),
            // Phase 4: South – all movements
            Set.of(
                    new Movement(Direction.SOUTH, Turn.LEFT),
                    new Movement(Direction.SOUTH, Turn.STRAIGHT),
                    new Movement(Direction.SOUTH, Turn.RIGHT)
            ),
            // Phase 5: East-West straight and right turn
            Set.of(
                    new Movement(Direction.EAST,  Turn.STRAIGHT),
                    new Movement(Direction.EAST,  Turn.RIGHT),
                    new Movement(Direction.WEST,  Turn.STRAIGHT),
                    new Movement(Direction.WEST,  Turn.RIGHT)
            ),
            // Phase 6: East-West left turn
            Set.of(
                    new Movement(Direction.EAST,  Turn.LEFT),
                    new Movement(Direction.WEST,  Turn.LEFT)
            ),
            // Phase 7: East – all movements
            Set.of(
                    new Movement(Direction.EAST,  Turn.LEFT),
                    new Movement(Direction.EAST,  Turn.STRAIGHT),
                    new Movement(Direction.EAST,  Turn.RIGHT)
            ),
            // Phase 8: West – all movements
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
