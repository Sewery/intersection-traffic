package com.seweryn.tasior.controller;

import com.seweryn.tasior.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReactiveWeightCalculatorTest {

    private ReactiveWeightCalculator calculator;
    private Intersection intersection;

    @BeforeEach
    void setUp() {
        calculator = new ReactiveWeightCalculator();
        intersection = new Intersection();
    }

    // wagi podstawowe

    @Test
    void emptyLaneShouldHaveZeroWeight() {
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        assertEquals(0.0, calculator.calculatePhaseWeight(phase, intersection, 5));
    }

    @Test
    void carShouldHaveDefaultPriorityOne() {
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        // waitTime=1, (1.0 + 1^1.5) * 1.0 = 2.0
        assertEquals(2.0, calculator.calculatePhaseWeight(phase, intersection, 1), 0.001);
    }

    @Test
    void busShouldHaveDefaultPriorityFive() {
        addVehicle("bus1", Direction.NORTH, Direction.SOUTH, 0);
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        // waitTime=1, (1.0 + 1^1.5) * 5.0 = 10.0
        assertEquals(10.0, calculator.calculatePhaseWeight(phase, intersection, 1), 0.001);
    }

    @Test
    void multipleVehiclesShouldSumWeights() {
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);
        addVehicle("car2", Direction.NORTH, Direction.SOUTH, 0);
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        // waitTime=1, 2 * (1.0 + 1^1.5) * 1.0 = 4.0
        assertEquals(4.0, calculator.calculatePhaseWeight(phase, intersection, 1), 0.001);
    }

    // konfiguracja
    @Test
    void configureShouldChangeCarPriority() {
        calculator.configure(3.0, 5.0);
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        // waitTime=1, (1.0 + 1^1.5) * 3.0 = 6.0
        assertEquals(6.0, calculator.calculatePhaseWeight(phase, intersection, 1), 0.001);
    }

    @Test
    void configureShouldChangeBusPriority() {
        calculator.configure(1.0, 10.0);
        addVehicle("bus1", Direction.NORTH, Direction.SOUTH, 0);
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        // waitTime=1, (1.0 + 1^1.5) * 10.0 = 20.0
        assertEquals(20.0, calculator.calculatePhaseWeight(phase, intersection, 1), 0.001);
    }

    // zagłodzenie

    @Test
    void vehicleWaitingMaxTimeShouldBeStarving() {
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);

        assertFalse(calculator.hasStarvingVehicle(intersection, 9),
                "waitTime=9 < MAX_WAIT_TIME – nie zagłodzony");
        assertTrue(calculator.hasStarvingVehicle(intersection, 10),
                "waitTime=10 >= MAX_WAIT_TIME – zagłodzony");
    }

    @Test
    void getStarvingMovementsShouldReturnCorrectLane() {
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);

        Set<Movement> starving = calculator.getStarvingMovements(intersection, 10);

        assertEquals(1, starving.size());
        assertTrue(starving.contains(new Movement(Direction.NORTH, Turn.STRAIGHT)),
                "Zagłodzony ruch powinien być NORTH→STRAIGHT");
    }

    //helper
    private void addVehicle(String vehicleId, Direction from, Direction to, int arrivalStep) {
        intersection.getRoad(from).addVehicleToLane(to, new Vehicle(vehicleId, arrivalStep));
    }
}