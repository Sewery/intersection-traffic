package com.seweryn.tasior;

import com.seweryn.tasior.io.SimulationResult;
import com.seweryn.tasior.io.StepStatus;
import com.seweryn.tasior.statistics.Statistics;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {

    private static final String RESOURCES = "src/test/resources/";

    // input1: podstawowy NS + EW
    @Test
    void input1_numberOfStepStatusesShouldMatchStepCommands() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input1.json");
        assertEquals(7, engine.runSimulation().getStepStatuses().size());
    }

    @Test
    void input1_allVehiclesShouldExit() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input1.json");
        assertEquals(4, countExited(engine.runSimulation()));
    }

    // input2: FIFO jeden pas

    @Test
    void input2_vehiclesShouldExitInFifoOrder() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input2.json");
        List<StepStatus> steps = engine.runSimulation().getStepStatuses();

        assertAll("FIFO order",
                () -> assertTrue(findExitStep(steps, "car1") < findExitStep(steps, "car2")),
                () -> assertTrue(findExitStep(steps, "car2") < findExitStep(steps, "car3")),
                () -> assertTrue(findExitStep(steps, "car3") < findExitStep(steps, "car4")),
                () -> assertTrue(findExitStep(steps, "car4") < findExitStep(steps, "car5"))
        );
    }

    @Test
    void input2_maxOneVehiclePerStepOnSameLane() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input2.json");
        engine.runSimulation().getStepStatuses().forEach(step ->
                assertTrue(step.leftVehicles().size() <= 1)
        );
    }

    @Test
    void input2_allVehiclesShouldExit() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input2.json");
        long totalExited = countExited(engine.runSimulation());

        assertEquals(5, totalExited);
    }

    // input3: brak kolizji NS i EW naprzemiennie

    @Test
    void input3_allVehiclesShouldExit() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input3.json");
        long totalExited = countExited(engine.runSimulation());

        assertEquals(8, totalExited);
    }

    @Test
    void input3_nsAndEwShouldNeverExitInSameStep() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input3.json");
        engine.runSimulation().getStepStatuses().forEach(step -> {
            boolean hasNS = step.leftVehicles().stream()
                    .anyMatch(id -> id.startsWith("ns_"));
            boolean hasEW = step.leftVehicles().stream()
                    .anyMatch(id -> id.startsWith("ew_"));
            assertFalse(hasNS && hasEW,
                    "Kolizja NS i EW: " + step.leftVehicles());
        });
    }

    // input4: puste skrzyżowanie

    @Test
    void input4_emptyIntersectionShouldReturnEmptySteps() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input4.json");
        engine.runSimulation().getStepStatuses().forEach(step ->
                assertTrue(step.leftVehicles().isEmpty())
        );
    }

    @Test
    void input4_shouldHaveFiveStepStatuses() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input4.json");
        SimulationResult result = engine.runSimulation();

        assertEquals(5, result.getStepStatuses().size());
    }

    // input5: wszystkie kierunki, brak kolizji

    @Test
    void input5_allVehiclesShouldExit() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input5.json");
        long totalExited = countExited(engine.runSimulation());

        assertEquals(9, totalExited);
    }

    @Test
    void input5_nsAndEwShouldNeverExitInSameStep() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input5.json");
        List<StepStatus> steps = engine.runSimulation().getStepStatuses();

        steps.forEach(step -> {
            boolean hasNS = step.leftVehicles().stream()
                    .anyMatch(id -> id.startsWith("n_") || id.startsWith("s_"));
            boolean hasEW = step.leftVehicles().stream()
                    .anyMatch(id -> id.startsWith("e_") || id.startsWith("w_"));

            assertFalse(hasNS && hasEW,
                    "Kolizja! NS i EW w tym samym stepie: " + step.leftVehicles());
        });
    }

    // input6: dynamiczny ruch + bus priority
    @Test
    void input6_allVehiclesShouldExit() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input6.json");
        long totalExited = countExited(engine.runSimulation());

        assertEquals(12, totalExited);
    }
    @Test
    void input6_busShouldExitBeforeCars() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input6.json");
        List<StepStatus> steps = engine.runSimulation().getStepStatuses();

        assertTrue(findExitStep(steps, "bus1") < findExitStep(steps, "car1"));
    }

    // input7: zagłodzenie - pojazd stoi, gdy z innej strony jedzie strumień aut
    @Test
    void input7_starvingVehicleShouldEventuallyExit() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input7.json");
        List<StepStatus> steps = engine.runSimulation().getStepStatuses();

        int exitStep = findExitStep(steps, "starving_car");
        assertNotEquals(-1, exitStep, "Zagłodzony pojazd musi wyjechać mimo dominującego strumienia NS");
    }

    @Test
    void input7_allVehiclesShouldExitDespiteContinuousStream() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input7.json");
        long totalExited = countExited(engine.runSimulation());

        assertEquals(11, totalExited);
    }

    //input 8: dynamiczny ruch w trakcie symulacji
    @Test
    void input8_allVehiclesShouldExit() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input8.json");
        assertEquals(4, countExited(engine.runSimulation()));
    }

    @Test
    void input8_vehiclesAddedBeforeShouldExitFirst() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input8.json");
        List<StepStatus> steps = engine.runSimulation().getStepStatuses();

        // car1 i car2 dodane przed stepami – powinny wyjechać przed car3
        assertAll(
                () -> assertTrue(findExitStep(steps, "car1") < findExitStep(steps, "car3")),
                () -> assertTrue(findExitStep(steps, "car2") < findExitStep(steps, "car3"))
        );
    }

    // input10: getStatistics – podstawowe statystyki
    @Test
    void input10_shouldReturnCorrectTotalVehiclesProcessed() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input10_statistics.json");
        SimulationResult result = engine.runSimulation();

        assertNotNull(result.getStatistics(), "Statystyki muszą być obliczone");
        assertEquals(4, result.getStatistics().totalVehiclesProcessed());
    }

    @Test
    void input10_shouldReturnCorrectAverageWaitTime() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input10_statistics.json");
        Statistics stats = engine.runSimulation().getStatistics();

        assertNotNull(stats);
        assertTrue(stats.averageWaitTime() > 0,
                "Średni czas oczekiwania musi być > 0");
    }

    @Test
    void input10_statisticsShouldBeNullWithoutGetStatisticsCommand() {
        // input1 nie ma getStatistics → statistics powinno być null
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input1.json");
        assertNull(engine.runSimulation().getStatistics(),
                "Statystyki powinny być null jeśli getStatistics nie było wywołane");
    }

    // input11: updateLaneStatus – blokowanie pasa
    @Test
    void input11_blockedLaneShouldPreventVehiclesFromExiting() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input11_block.json");
        SimulationResult result = engine.runSimulation();
        Statistics stats = result.getStatistics();

        assertNotNull(stats);
        assertTrue(stats.totalVehiclesStuck() > 0,
                "Pojazdy na zablokowanym pasie powinny być stuck");
    }

    @Test
    void input11_blockedLaneShouldHaveFromAndToStep() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input11_block.json");
        Statistics stats = engine.runSimulation().getStatistics();

        assertNotNull(stats);
        assertFalse(stats.blockedLanes().isEmpty(), "Musi być zablokowany pas");

        Statistics.BlockedLaneStat blocked = stats.blockedLanes().get(0);
        assertTrue(blocked.blockedFromStep() >= 0, "fromStep musi być >= 0");
        assertEquals(-1, blocked.blockedToStep(),
                "Pas nadal zablokowany → toStep=-1");
    }

    @Test
    void input11_unblockedLaneShouldHaveToStep() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input11_unblock.json");
        Statistics stats = engine.runSimulation().getStatistics();

        assertNotNull(stats);
        Statistics.BlockedLaneStat blocked = stats.blockedLanes().get(0);
        assertNotEquals(-1, blocked.blockedToStep(),
                "Pas odblokowany → toStep != -1");
        assertTrue(blocked.blockedToStep() > blocked.blockedFromStep(),
                "toStep musi być po fromStep");
    }

    // helpers
    private int findExitStep(List<StepStatus> steps, String vehicleId) {
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).leftVehicles().contains(vehicleId)) return i;
        }
        return -1;
    }

    private long countExited(SimulationResult result) {
        return result.getStepStatuses().stream()
                .mapToLong(s -> s.leftVehicles().size())
                .sum();
    }

}
