package com.seweryn.tasior.controller;

import com.seweryn.tasior.controller.TrafficController;
import com.seweryn.tasior.controller.ReactiveWeightCalculator;
import com.seweryn.tasior.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TrafficControllerTest {
    private Intersection intersection;
    private TrafficController controller;

    @BeforeEach
    void setUp() {
        intersection = new Intersection();
        controller = new TrafficController(intersection, new ReactiveWeightCalculator());
    }

    // initial state

    @Test
    void initialPhaseShouldBeNull() {
        assertNull(controller.getCurrentPhase());
    }

    @Test
    void firstStepShouldSetPhase() {
        controller.executeStep(0);
        assertNotNull(controller.getCurrentPhase());
    }

    @Test
    void firstPhaseShouldHaveGreenLights() {
        controller.executeStep(0);

        controller.getCurrentPhase().forEach(movement -> {
            Lane lane = intersection.getRoad(movement.from())
                    .getLaneByTurn(movement.turn())
                    .orElseThrow();
            assertTrue(lane.getTrafficLight().isGreen());
        });
    }

    // safety

    @Test
    void conflictingDirectionsShouldNeverHaveGreenSimultaneously() {
        controller.executeStep(0);

        Set<Movement> phase = controller.getCurrentPhase();
        boolean hasNS = phase.stream().anyMatch(m ->
                m.from() == Direction.NORTH || m.from() == Direction.SOUTH);
        boolean hasEW = phase.stream().anyMatch(m ->
                m.from() == Direction.EAST || m.from() == Direction.WEST);

        assertFalse(hasNS && hasEW);
    }

    // phase switching

    @Test
    void phaseShouldNotSwitchWhenCurrentPhaseIsHeavier() {
        addVehicles(Direction.SOUTH, Direction.NORTH, 5);
        controller.executeStep(0);
        Set<Movement> initialPhase = controller.getCurrentPhase();

        controller.executeStep(1);

        assertEquals(initialPhase, controller.getCurrentPhase());
    }


    @Test
    void phaseShouldSwitchWhenOtherSideIsMuchHeavier() {
        addVehicles(Direction.NORTH, Direction.SOUTH, 1);
        controller.executeStep(0);

        addVehicles(Direction.EAST, Direction.WEST, 20);
        controller.executeStep(1);

        // wait out yellow transition
        for (int i = 2; i < 2 + TrafficDefaults.YELLOW_TIME * 2; i++) {
            controller.executeStep(i);
        }

        Set<Movement> phase = controller.getCurrentPhase();
        boolean isEW = phase.stream().anyMatch(m ->
                m.from() == Direction.EAST || m.from() == Direction.WEST);
        assertTrue(isEW);
    }

    @Test
    void starvingVehicleShouldEventuallyGetGreen() {
        addVehicles(Direction.NORTH, Direction.SOUTH, 10);
        addVehicles(Direction.EAST,  Direction.WEST,  1);

        boolean ewEverGotGreen = false;

        for (int i = 0; i <= 50; i++) {
            // finish crossing
            intersection.getRoads().forEach(road ->
                    road.getLanes().forEach(Lane::finishCrossing));

            controller.executeStep(i);

            // start crossing
            intersection.getRoads().forEach(road ->
                    road.getLanes().stream()
                            .filter(lane -> lane.isPassable() && !lane.isEmpty() && !lane.isCrossing())
                            .forEach(Lane::startCrossing));

            Set<Movement> phase = controller.getCurrentPhase();
            if (phase.stream().anyMatch(m ->
                    m.from() == Direction.EAST || m.from() == Direction.WEST)) {
                ewEverGotGreen = true;
                break;
            }
        }

        assertTrue(ewEverGotGreen);
    }
    // helpers

    private void addVehicles(Direction from, Direction to, int count) {
        for (int i = 0; i < count; i++) {
            intersection.getRoad(from).addVehicleToLane(to,
                    new Vehicle("car_" + from + "_" + i, 0));
        }
    }

}