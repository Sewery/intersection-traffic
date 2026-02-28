import com.seweryn.tasior.SimulationEngine;
import com.seweryn.tasior.io.SimulationResult;
import com.seweryn.tasior.io.StepStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class AlgorithmComparisonTest {

    private static final String RESOURCES = "src/test/resources/";

    @Test
    void historical_shouldHaveGreenForEW_beforeVehiclesArrive() {
        // Stepy 0-1: brak pojazdów
        // HISTORICAL: EW factor=5.0, EW ma zielone od razu
        // REACTIVE:   brak pojazdów, nie wie co wybrać → NS lub EW losowo
        //
        // Gdy pojazdy EW pojawiają się na stepie 2:
        // HISTORICAL: już ma zielone, e_car1 jedzie od razu
        // REACTIVE:   musi przełączyć na EW, traci YELLOW_TIME stepów

        SimulationEngine historical = new SimulationEngine(RESOURCES + "input9_historical.json");
        SimulationEngine reactive   = new SimulationEngine(RESOURCES + "input9_reactive.json");

        List<StepStatus> historicalSteps = historical.runSimulation().getStepStatuses();
        List<StepStatus> reactiveSteps   = reactive.runSimulation().getStepStatuses();

        int historicalExit = findExitStep(historicalSteps, "e_car1");
        int reactiveExit   = findExitStep(reactiveSteps,   "e_car1");

        assertNotEquals(-1, historicalExit, "e_car1 musi wyjechać w HISTORICAL");
        assertNotEquals(-1, reactiveExit,   "e_car1 musi wyjechać w REACTIVE");

        assertTrue(historicalExit < reactiveExit,
                "HISTORICAL powinno obsłużyć e_car1 szybciej bo zielone było gotowe zanim pojazd przyjechał " +
                "(historical=" + historicalExit + " reactive=" + reactiveExit + ")");
    }

    @Test
    void historical_shouldSustainGreenForEW_insteadOfSwitching() {
        // 3 pojazdy EW vs 3 pojazdy NS – równa liczba
        // REACTIVE: po obsłużeniu EW może przełączyć na NS bo wagi są równe
        // HISTORICAL: EW factor=5.0 >> NS factor=0.1, podtrzymuje zielone dla EW
        // wszystkie e_car wyjeżdżają zanim n_car dostanie zielone

        SimulationEngine historical = new SimulationEngine(RESOURCES + "input9_historical.json");
        SimulationEngine reactive   = new SimulationEngine(RESOURCES + "input9_reactive.json");

        List<StepStatus> historicalSteps = historical.runSimulation().getStepStatuses();
        List<StepStatus> reactiveSteps   = reactive.runSimulation().getStepStatuses();

        // HISTORICAL: wszystkie e_car wyjeżdżają przed n_car
        int historicalLastEWExit = findExitStep(historicalSteps, "e_car3");
        int historicalFirstNSExit = findExitStep(historicalSteps, "n_car1");

        assertTrue(historicalLastEWExit < historicalFirstNSExit,
                "HISTORICAL: wszystkie EW powinny wyjechać zanim NS dostanie zielone");

        // REACTIVE: może przeplatać EW i NS bo wagi są równe po kilku stepach
        int reactiveLastEWExit   = findExitStep(reactiveSteps, "e_car3");
        int reactiveFirstNSExit  = findExitStep(reactiveSteps, "n_car1");

        assertFalse(reactiveLastEWExit < reactiveFirstNSExit,
                "REACTIVE: nie powinno gwarantować że wszystkie EW wyjadą przed NS");
    }

    @Test
    void historical_shouldClearAllEWVehiclesFaster() {
        // porównanie całkowitego czasu obsługi wszystkich pojazdów EW
        SimulationEngine historical = new SimulationEngine(RESOURCES + "input9_historical.json");
        SimulationEngine reactive   = new SimulationEngine(RESOURCES + "input9_reactive.json");

        List<StepStatus> historicalSteps = historical.runSimulation().getStepStatuses();
        List<StepStatus> reactiveSteps   = reactive.runSimulation().getStepStatuses();

        int historicalTotalEWSteps = totalWaitSteps(historicalSteps, List.of("e_car1", "e_car2", "e_car3"));
        int reactiveTotalEWSteps   = totalWaitSteps(reactiveSteps,   List.of("e_car1", "e_car2", "e_car3"));

        System.out.printf("[total EW wait] HISTORICAL=%d REACTIVE=%d (różnica=%d stepów)%n",
                historicalTotalEWSteps, reactiveTotalEWSteps,
                reactiveTotalEWSteps - historicalTotalEWSteps);

        assertTrue(historicalTotalEWSteps < reactiveTotalEWSteps,
                "HISTORICAL powinno obsłużyć wszystkie pojazdy EW w mniejszej sumie stepów " +
                        "(historical=" + historicalTotalEWSteps + " reactive=" + reactiveTotalEWSteps + ")");
    }

    @Test
    void allVehiclesShouldExitInBothModes() {
        SimulationEngine historical = new SimulationEngine(RESOURCES + "input9_historical.json");
        SimulationEngine reactive   = new SimulationEngine(RESOURCES + "input9_reactive.json");

        assertEquals(6, countExited(historical.runSimulation()),
                "HISTORICAL: wszystkie 6 pojazdów musi wyjechać");
        assertEquals(6, countExited(reactive.runSimulation()),
                "REACTIVE: wszystkie 6 pojazdów musi wyjechać");
    }

    //helpers
    private int findExitStep(List<StepStatus> steps, String vehicleId) {
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).leftVehicles().contains(vehicleId)) return i;
        }
        return -1;
    }

    /** Suma stepów wyjazdu dla listy pojazdów – im mniejsza tym lepiej */
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