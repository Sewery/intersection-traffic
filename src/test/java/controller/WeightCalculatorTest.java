package controller;

import com.seweryn.tasior.controller.WeightCalculator;
import com.seweryn.tasior.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WeightCalculatorTest {

    private WeightCalculator calculator;
    private Intersection intersection;

    @BeforeEach
    void setUp() {
        calculator = new WeightCalculator();
        intersection = new Intersection();
    }

    @Test
    void emptyLaneShouldHaveZeroWeight() {
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));
        assertEquals(0.0, calculator.calculatePhaseWeight(phase, intersection, 5));
    }

    @Test
    void carShouldHavePriorityOne() {
        intersection.getRoad(Direction.NORTH).addVehicleToLane(
                Direction.SOUTH, new Vehicle(0, "car1")
        );
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        // step=1, arrivalTime=0, diff=1 → 1^1.5 * 1.0 = 1.0
        double weight = calculator.calculatePhaseWeight(phase, intersection, 1);
        assertEquals(1.0, weight, 0.001);
    }

    @Test
    void busShouldHavePriorityFive() {
        intersection.getRoad(Direction.NORTH).addVehicleToLane(
                Direction.SOUTH, new Vehicle(0, "bus1")
        );
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        // step=1, arrivalTime=0, diff=1 → 1^1.5 * 5.0 = 5.0
        double weight = calculator.calculatePhaseWeight(phase, intersection, 1);
        assertEquals(5.0, weight, 0.001);
    }

    @Test
    void weightShouldGrowWithTime() {
        intersection.getRoad(Direction.NORTH).addVehicleToLane(
                Direction.SOUTH, new Vehicle(0, "car1")
        );
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        double weightAt1 = calculator.calculatePhaseWeight(phase, intersection, 1);
        double weightAt5 = calculator.calculatePhaseWeight(phase, intersection, 5);

        assertTrue(weightAt5 > weightAt1);
    }

    @Test
    void configureShouldChangePriorities() {
        calculator.configure(2.0, 10.0);
        intersection.getRoad(Direction.NORTH).addVehicleToLane(
                Direction.SOUTH, new Vehicle(0, "bus1")
        );
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        // step=1, diff=1 → 1^1.5 * 10.0 = 10.0
        double weight = calculator.calculatePhaseWeight(phase, intersection, 1);
        assertEquals(10.0, weight, 0.001);
    }

    @Test
    void multipleVehiclesShouldSumWeights() {
        intersection.getRoad(Direction.NORTH).addVehicleToLane(
                Direction.SOUTH, new Vehicle(0, "car1")
        );
        intersection.getRoad(Direction.NORTH).addVehicleToLane(
                Direction.SOUTH, new Vehicle(0, "car2")
        );
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        // step=1 → 2 * (1^1.5 * 1.0) = 2.0
        double weight = calculator.calculatePhaseWeight(phase, intersection, 1);
        assertEquals(2.0, weight, 0.001);
    }
}
