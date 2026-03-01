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

    // base weights

    @Test
    void emptyLaneShouldHaveZeroWeight() {
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));
        assertEquals(0.0, calculator.calculatePhaseWeight(phase, intersection, 5));
    }


    @Test
    void weightShouldScaleWithVehicleCount() {
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));
        double single = calculator.calculatePhaseWeight(phase, intersection, 1);

        addVehicle("car2", Direction.NORTH, Direction.SOUTH, 0);
        double multiple = calculator.calculatePhaseWeight(phase, intersection, 1);

        assertEquals(single * 2, multiple, 0.001);
    }


    // configure

    @Test
    void configureShouldAffectCarAndBusPriority() {
        calculator.configure(3.0, 10.0);

        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);
        addVehicle("bus1", Direction.EAST, Direction.WEST, 0, VehicleType.BUS);

        Set<Movement> nPhase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));
        Set<Movement> ePhase = Set.of(new Movement(Direction.EAST,  Turn.STRAIGHT));

        double carWeight = calculator.calculatePhaseWeight(nPhase, intersection, 1);
        double busWeight = calculator.calculatePhaseWeight(ePhase, intersection, 1);

        // bus priority 10 vs car priority 3
        assertTrue(busWeight > carWeight);
    }

    // starvation

    @Test
    void vehicleWaitingMaxTimeShouldBeStarving() {
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);
        assertFalse(calculator.hasStarvingVehicle(intersection, 9));
        assertTrue(calculator.hasStarvingVehicle(intersection, 10));
    }

    @Test
    void getStarvingMovementsShouldReturnCorrectLane() {
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);
        Set<Movement> starving = calculator.getStarvingMovements(intersection, 10);

        assertEquals(1, starving.size());
        assertTrue(starving.contains(new Movement(Direction.NORTH, Turn.STRAIGHT)));
    }

    @Test
    void starvingVehicleShouldGetMassiveWeightBoost() {
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        double before = calculator.calculatePhaseWeight(phase, intersection, 9);
        double after  = calculator.calculatePhaseWeight(phase, intersection, 10);

        assertTrue(after > before * 100);
    }

    // helpers

    private void addVehicle(String vehicleId, Direction from, Direction to, int arrivalStep) {
        addVehicle(vehicleId, from, to, arrivalStep, VehicleType.CAR);
    }

    private void addVehicle(String vehicleId, Direction from, Direction to, int arrivalStep, VehicleType type) {
        intersection.getRoad(from).addVehicleToLane(to, new Vehicle(vehicleId, arrivalStep, type));
    }
}