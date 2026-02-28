package com.seweryn.tasior.controller;

import com.seweryn.tasior.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HistoricalWeightCalculatorTest {

    private Intersection intersection;

    @BeforeEach
    void setUp() {
        intersection = new Intersection();
    }
    // helpers

    private HistoricalWeightCalculator calculatorWith(Map<Direction, List<TimeSlot>> data) {
        return new HistoricalWeightCalculator(data);
    }

    private void addVehicle(String vehicleId, Direction from, Direction to, int arrivalStep) {
        intersection.getRoad(from).addVehicleToLane(to, new Vehicle(vehicleId, arrivalStep));
    }

    // factor

    @Test
    void factorShouldAmplifyReactiveWeight() {
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);
        HistoricalWeightCalculator calculator = calculatorWith(Map.of(
                Direction.NORTH, List.of(new TimeSlot(0, 10, 3.0))
        ));
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        double reactive = new ReactiveWeightCalculator()
                .calculatePhaseWeight(phase, intersection, 1);
        double historical = calculator.calculatePhaseWeight(phase, intersection, 1);

        assertEquals(reactive * 3.0, historical, 0.001,
                "Historical powinno = reactive * factor");
    }

    @Test
    void factorOutsideSlotShouldDefaultToOne() {
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);
        HistoricalWeightCalculator calculator = calculatorWith(Map.of(
                Direction.NORTH, List.of(new TimeSlot(5, 10, 3.0))  // slot od stepu 5
        ));
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        double reactive = new ReactiveWeightCalculator()
                .calculatePhaseWeight(phase, intersection, 1);  // step=1, poza slotem
        double historical = calculator.calculatePhaseWeight(phase, intersection, 1);

        assertEquals(reactive, historical, 0.001,
                "Poza slotem factor powinien defaultować do 1.0");
    }

    @Test
    void factorShouldApplyCorrectSlotForStep() {
        HistoricalWeightCalculator calculator = calculatorWith(Map.of(
                Direction.NORTH, List.of(
                        new TimeSlot(0, 4,  2.0),
                        new TimeSlot(5, 10, 5.0)
                )
        ));
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        // osobny intersection dla każdego stepu – unikamy starvation
        Intersection intersection1 = new Intersection();
        intersection1.getRoad(Direction.NORTH).addVehicleToLane(Direction.SOUTH,
                new Vehicle("car1", 1));  // arrivalStep=1 -> czeka 0 stepów przy step=1

        Intersection intersection2 = new Intersection();
        intersection2.getRoad(Direction.NORTH).addVehicleToLane(Direction.SOUTH,
                new Vehicle("car2", 6));  // arrivalStep=6 -> czeka 0 stepów przy step=6

        ReactiveWeightCalculator reactive = new ReactiveWeightCalculator();
        double reactiveWeight1 = reactive.calculatePhaseWeight(phase, intersection1, 1);
        double reactiveWeight2 = reactive.calculatePhaseWeight(phase, intersection2, 6);

        assertEquals(reactiveWeight1 * 2.0, calculator.calculatePhaseWeight(phase, intersection1, 1), 0.001,
                "Step=1 powinien użyć pierwszego slotu (factor=2.0)");
        assertEquals(reactiveWeight2 * 5.0, calculator.calculatePhaseWeight(phase, intersection2, 6), 0.001,
                "Step=6 powinien użyć drugiego slotu (factor=5.0)");
    }

    // brak pojazdów

    @Test
    void emptyLaneShouldReturnFactorAsBaseWeight() {
        HistoricalWeightCalculator calculator = calculatorWith(Map.of(
                Direction.NORTH, List.of(new TimeSlot(0, 10, 4.0))
        ));
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        double weight = calculator.calculatePhaseWeight(phase, intersection, 1);

        assertEquals(4.0, weight, 0.001,
                "Brak pojazdów → waga = factor (baza historyczna)");
    }

    @Test
    void emptyLaneWithNoSlotShouldReturnOne() {
        HistoricalWeightCalculator calculator = calculatorWith(Map.of());
        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));

        double weight = calculator.calculatePhaseWeight(phase, intersection, 1);

        assertEquals(1.0, weight, 0.001,
                "Brak pojazdów i brak slotu → waga = 1.0 (default factor)");
    }

    //  konfiguracja

    @Test
    void configureShouldPropagateToReactive() {
        addVehicle("bus1", Direction.NORTH, Direction.SOUTH, 0);
        HistoricalWeightCalculator calculator = calculatorWith(Map.of(
                Direction.NORTH, List.of(new TimeSlot(0, 10, 2.0))
        ));
        calculator.configure(1.0, 10.0);  // busPriority=10.0

        Set<Movement> phase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));
        double weight = calculator.calculatePhaseWeight(phase, intersection, 1);

        ReactiveWeightCalculator reactive = new ReactiveWeightCalculator();
        reactive.configure(1.0, 10.0);
        double expectedReactive = reactive.calculatePhaseWeight(phase, intersection, 1);

        assertEquals(expectedReactive * 2.0, weight, 0.001,
                "configure powinno być przekazane do reactive");
    }

    // starvation

    @Test
    void starvingVehicleShouldOverrideHistoricalFactor() {
        // NS factor=0.1 – normalnie NS prawie nigdy nie wygrywa
        // ale po MAX_WAIT_TIME starvation powinno nadpisać factor
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);
        HistoricalWeightCalculator calculator = calculatorWith(Map.of(
                Direction.NORTH, List.of(new TimeSlot(0, 20, 0.1)),
                Direction.EAST,  List.of(new TimeSlot(0, 20, 5.0))
        ));

        Set<Movement> nsPhase = Set.of(new Movement(Direction.NORTH, Turn.STRAIGHT));
        Set<Movement> ewPhase = Set.of(new Movement(Direction.EAST,  Turn.STRAIGHT));

        // przed starvation – EW wygrywa
        double nsWeight = calculator.calculatePhaseWeight(nsPhase, intersection, 1);
        double ewWeight = calculator.calculatePhaseWeight(ewPhase, intersection, 1);
        assertTrue(ewWeight > nsWeight, "Przed starvation EW powinno wygrać");

        // po starvation – NS powinno dostać boost
        double nsStarving = calculator.calculatePhaseWeight(nsPhase, intersection, 10);
        assertTrue(nsStarving > ewWeight,
                "Po starvation NS powinno nadpisać historical factor");
    }

    @Test
    void hasStarvingVehicleShouldDelegateToReactive() {
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);
        HistoricalWeightCalculator calculator = calculatorWith(Map.of());

        assertFalse(calculator.hasStarvingVehicle(intersection, 9));
        assertTrue(calculator.hasStarvingVehicle(intersection, 10));
    }

    @Test
    void getStarvingMovementsShouldDelegateToReactive() {
        addVehicle("car1", Direction.NORTH, Direction.SOUTH, 0);
        HistoricalWeightCalculator calculator = calculatorWith(Map.of());

        Set<Movement> starving = calculator.getStarvingMovements(intersection, 10);

        assertEquals(1, starving.size());
        assertTrue(starving.contains(new Movement(Direction.NORTH, Turn.STRAIGHT)));
    }
}
