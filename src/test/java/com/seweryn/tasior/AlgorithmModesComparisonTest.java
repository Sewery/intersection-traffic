package com.seweryn.tasior;

import com.seweryn.tasior.io.SimulationResult;
import com.seweryn.tasior.io.StepStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class AlgorithmModesComparisonTest {

    private static final String RESOURCES = "src/test/resources/";

    // steps 0-1: no vehicles yet
    // HISTORICAL: EW factor=5.0 → green from the start
    // REACTIVE:   no vehicles → picks NS or EW arbitrarily
    // when EW vehicles arrive at step 2:
    // HISTORICAL: already green, e_car1 goes immediately
    // REACTIVE:   must switch to EW, loses YELLOW_TIME steps
    @Test
    void historical_shouldHaveGreenForEW_beforeVehiclesArrive() {
        SimulationEngine historical = new SimulationEngine(RESOURCES + "input9_historical.json");
        SimulationEngine reactive   = new SimulationEngine(RESOURCES + "input9_reactive.json");

        int historicalExit = findExitStep(historical.runSimulation().getStepStatuses(), "e_car1");
        int reactiveExit   = findExitStep(reactive.runSimulation().getStepStatuses(),   "e_car1");

        assertNotEquals(-1, historicalExit);
        assertNotEquals(-1, reactiveExit);
        assertTrue(historicalExit < reactiveExit);
    }

    // 3 EW vs 3 NS vehicles – equal count
    // REACTIVE:   may switch to NS after EW served (equal weights)
    // HISTORICAL: EW factor=5.0 > NS factor=0.1 → keeps green for EW
    //             all e_cars exit before n_cars get green
    @Test
    void historical_shouldSustainGreenForEW_insteadOfSwitching() {
        SimulationEngine historical = new SimulationEngine(RESOURCES + "input9_historical.json");
        SimulationEngine reactive   = new SimulationEngine(RESOURCES + "input9_reactive.json");

        List<StepStatus> historicalSteps = historical.runSimulation().getStepStatuses();
        List<StepStatus> reactiveSteps   = reactive.runSimulation().getStepStatuses();

        assertTrue(findExitStep(historicalSteps, "e_car3") < findExitStep(historicalSteps, "n_car1"));
        assertFalse(findExitStep(reactiveSteps, "e_car3")  < findExitStep(reactiveSteps,   "n_car1"));
    }

    // total steps to clear all EW vehicles
    @Test
    void historical_shouldClearAllEWVehiclesFaster() {
        SimulationEngine historical = new SimulationEngine(RESOURCES + "input9_historical.json");
        SimulationEngine reactive   = new SimulationEngine(RESOURCES + "input9_reactive.json");

        int historicalTotal = totalWaitSteps(historical.runSimulation().getStepStatuses(), List.of("e_car1", "e_car2", "e_car3"));
        int reactiveTotal   = totalWaitSteps(reactive.runSimulation().getStepStatuses(),   List.of("e_car1", "e_car2", "e_car3"));

        assertTrue(historicalTotal < reactiveTotal);
    }

    @Test
    void allVehiclesShouldExitInBothModes() {
        SimulationEngine historical = new SimulationEngine(RESOURCES + "input9_historical.json");
        SimulationEngine reactive   = new SimulationEngine(RESOURCES + "input9_reactive.json");

        assertEquals(6, countExited(historical.runSimulation()));
        assertEquals(6, countExited(reactive.runSimulation()));
    }

    // helpers

    private int findExitStep(List<StepStatus> steps, String vehicleId) {
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).leftVehicles().contains(vehicleId)) return i;
        }
        return -1;
    }

    private int totalWaitSteps(List<StepStatus> steps, List<String> vehicleIds) {
        return vehicleIds.stream()
                .mapToInt(id -> findExitStep(steps, id))
                .sum();
    }

    private long countExited(SimulationResult result) {
        return result.getStepStatuses().stream()
                .mapToLong(s -> s.leftVehicles().size())
                .sum();
    }
}