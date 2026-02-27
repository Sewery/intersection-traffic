package controller;

import com.seweryn.tasior.controller.TrafficController;
import com.seweryn.tasior.controller.WeightCalculator;
import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.Intersection;
import com.seweryn.tasior.model.TrafficLight;
import com.seweryn.tasior.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrafficControllerTest {

    private Intersection intersection;
    private TrafficController controller;

    @BeforeEach
    void setUp() {
        intersection = new Intersection();
        controller = new TrafficController(intersection, new WeightCalculator());
    }

    @Test
    void firstPhaseShouldBeSetImmediately() {
        intersection.getRoad(Direction.NORTH).addVehicleToLane(
                Direction.SOUTH, new Vehicle(0, "car1")
        );
        controller.executeStep(1);

        assertNotNull(controller.getCurrentPhase());
    }

    @Test
    void firstPhaseShouldHaveGreenLights() {
        intersection.getRoad(Direction.NORTH).addVehicleToLane(
                Direction.SOUTH, new Vehicle(0, "car1")
        );
        controller.executeStep(1);

        boolean anyGreen = intersection.getRoads().stream()
                .flatMap(road -> road.getLanes().stream())
                .anyMatch(lane -> lane.getTrafficLight().isGreen());

        assertTrue(anyGreen);
    }

    @Test
    void emptyIntersectionShouldNotCrash() {
        assertDoesNotThrow(() -> controller.executeStep(1));
    }

    @Test
    void busHigherPriorityShouldSwitchPhase() {
        // NS ma 3 samochody
        for (int i = 0; i < 3; i++) {
            intersection.getRoad(Direction.NORTH).addVehicleToLane(
                    Direction.SOUTH, new Vehicle(0, "car" + i)
            );
        }

        // ustaw pierwszą fazę NS
        controller.executeStep(1);

        // dodaj busa na EW – wyższy priorytet
        intersection.getRoad(Direction.WEST).addVehicleToLane(
                Direction.EAST, new Vehicle(1, "bus1")
        );

        // poczekaj aż waga busa przewyższy NS
        for (int step = 2; step < 20; step++) {
            controller.executeStep(step);
        }

        // faza powinna się zmienić na EW
        boolean ewPhaseActive = controller.getCurrentPhase().stream()
                .anyMatch(m -> m.from() == Direction.WEST || m.from() == Direction.EAST);

        assertTrue(ewPhaseActive);
    }

    @Test
    void shouldNotSwitchWhenCurrentPhaseIsBest() {
        intersection.getRoad(Direction.NORTH).addVehicleToLane(
                Direction.SOUTH, new Vehicle(0, "car1")
        );
        intersection.getRoad(Direction.NORTH).addVehicleToLane(
                Direction.SOUTH, new Vehicle(0, "car2")
        );

        controller.executeStep(1);
        var phaseAfterFirst = controller.getCurrentPhase();

        controller.executeStep(2);
        var phaseAfterSecond = controller.getCurrentPhase();

        assertEquals(phaseAfterFirst, phaseAfterSecond);
    }

    @Test
    void transitionShouldUsYellowLight() {
        // ustaw NS
        intersection.getRoad(Direction.NORTH).addVehicleToLane(
                Direction.SOUTH, new Vehicle(0, "car1")
        );
        controller.executeStep(1);

        // dodaj dużo pojazdów na EW żeby wymusić zmianę
        for (int i = 0; i < 5; i++) {
            intersection.getRoad(Direction.WEST).addVehicleToLane(
                    Direction.EAST, new Vehicle(1, "bus" + i)
            );
        }

        controller.executeStep(2);

        boolean anyYellow = intersection.getRoads().stream()
                .flatMap(road -> road.getLanes().stream())
                .anyMatch(lane -> lane.getTrafficLight().getState() == TrafficLight.State.YELLOW_TO_RED
                        || lane.getTrafficLight().getState() == TrafficLight.State.YELLOW_TO_GREEN);

        assertTrue(anyYellow);
    }
}