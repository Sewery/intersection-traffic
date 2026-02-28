package controller;

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

    // inicjalizacja
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
            assertTrue(lane.getTrafficLight().isGreen(),
                    "Pas w aktywnej fazie powinien mieć zielone: " + movement);
        });
    }

    // bezpieczenstwo
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

    // zmiana fazy
    @Test
    void phaseShouldNotSwitchWhenCurrentPhaseIsHeavier() {
        addVehicles("south", "north", 5);
        controller.executeStep(0);
        Set<Movement> initialPhase = controller.getCurrentPhase();

        controller.executeStep(1);

        assertEquals(initialPhase, controller.getCurrentPhase());
    }


    // helpers
    private void addVehicles(String from, String to, int count) {
        Direction start = Direction.fromString(from);
        Direction end = Direction.fromString(to);
        for (int i = 0; i < count; i++) {
            intersection.getRoad(start).addVehicleToLane(end,
                    new Vehicle("car_" + from + "_" + i, 0));
        }
    }
}