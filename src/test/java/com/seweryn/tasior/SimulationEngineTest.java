package com.seweryn.tasior;

import com.seweryn.tasior.io.SimulationResult;
import com.seweryn.tasior.io.StepStatus;
import com.seweryn.tasior.statistics.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {

    private static final String RESOURCES = "src/test/resources/";


    @ParameterizedTest
    @CsvSource({
            "input1.json, 4",
            "input2.json, 5",
            "input3.json, 8",
            "input4.json, 0",
            "input5.json, 9",
            "input6.json, 12",
            "input7.json, 11",
            "input8.json, 4",
    })
    void allVehiclesShouldExit(String file, int expected) {
        SimulationEngine engine = new SimulationEngine(RESOURCES + file);
        assertEquals(expected, countExited(engine.runSimulation()));
    }

    // input1: basic NS + EW

    @Test
    void vehicleType_defaultShouldBeCar() {
        assertDoesNotThrow(() ->
                new SimulationEngine(RESOURCES + "input1.json").runSimulation()
        );
    }

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

    // input2: FIFO single lane

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

    // input3: no collision, NS and EW alternate

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

    // input5: all directions, no collision

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

    // input6: dynamic traffic + bus priority
    @Test
    void input6_busShouldExitBeforeCars() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input6.json");
        List<StepStatus> steps = engine.runSimulation().getStepStatuses();

        assertTrue(findExitStep(steps, "bus1") < findExitStep(steps, "car1"));
    }

    // input7: starvation – vehicle stuck while stream dominates from other direction

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

    // input8: dynamic traffic during simulation

    @Test
    void input8_allVehiclesShouldExit() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input8.json");
        assertEquals(4, countExited(engine.runSimulation()));
    }

    @Test
    void input8_vehiclesAddedBeforeShouldExitFirst() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input8.json");
        List<StepStatus> steps = engine.runSimulation().getStepStatuses();

        assertAll(
                () -> assertTrue(findExitStep(steps, "car1") < findExitStep(steps, "car3")),
                () -> assertTrue(findExitStep(steps, "car2") < findExitStep(steps, "car3"))
        );
    }

    // input10: getStatistics

    @Test
    void input10_shouldReturnCorrectTotalVehiclesProcessed() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input10_statistics.json");
        SimulationResult result = engine.runSimulation();
        assertNotNull(result.getStatistics());
        assertEquals(4, result.getStatistics().totalVehiclesProcessed());
    }

    @Test
    void input10_shouldReturnCorrectAverageWaitTime() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input10_statistics.json");
        Statistics stats = engine.runSimulation().getStatistics();
        assertNotNull(stats);
        assertTrue(stats.averageWaitTime() >= 1.0 && stats.averageWaitTime() <= 8.0);
    }

    @Test
    void input10_statisticsShouldBeNullWithoutGetStatisticsCommand() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input1.json");
        assertNull(engine.runSimulation().getStatistics());
    }

    // input11: updateLaneStatus – lane blocking

    @Test
    void input11_blockedLaneShouldPreventVehiclesFromExiting() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input11_block.json");
        assertTrue(engine.runSimulation().getStatistics().totalVehiclesStuck() > 0);
    }

    @Test
    void input11_vehiclesShouldExitAfterUnblock() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input11_unblock.json");
        assertTrue(countExited(engine.runSimulation()) > 0);
    }

    @Test
    void input11_blockedLaneShouldHaveFromAndToStep() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input11_block.json");
        Statistics stats = engine.runSimulation().getStatistics();
        Statistics.BlockedLaneStat blocked = stats.blockedLanes().get(0);
        assertTrue(blocked.blockedFromStep() >= 0);
        assertEquals(-1, blocked.blockedToStep());
    }

    @Test
    void input11_unblockedLaneShouldHaveToStep() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input11_unblock.json");
        Statistics stats = engine.runSimulation().getStatistics();
        Statistics.BlockedLaneStat blocked = stats.blockedLanes().get(0);
        assertNotEquals(-1, blocked.blockedToStep());
        assertTrue(blocked.blockedToStep() > blocked.blockedFromStep());
    }


    // input_low_maxwait – starvation triggered earlier with lower maxWaitTime

    @Test
    void configure_lowMaxWaitTime_shouldTriggerStarvationFaster() {
        SimulationEngine engineLow     = new SimulationEngine(RESOURCES + "input_low_maxwait.json");
        SimulationEngine engineDefault = new SimulationEngine(RESOURCES + "input_default_maxwait.json");

        int exitLow     = findExitStep(engineLow.runSimulation().getStepStatuses(),"starving_car");
        int exitDefault = findExitStep(engineDefault.runSimulation().getStepStatuses(), "starving_car");

        assertNotEquals(-1, exitLow);
        assertNotEquals(-1, exitDefault);
        assertTrue(exitLow < exitDefault);
    }

    // input_partial_configure – partial algorithm reconfiguration

    @Test
    void partialConfigure_updatedFieldShouldTakeEffect() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input_partial_configure.json");
        List<StepStatus> steps = engine.runSimulation().getStepStatuses();

        int bus2Exit = findExitStep(steps, "bus2");
        int car2Exit = findExitStep(steps, "car2");

        assertNotEquals(-1, bus2Exit);
        assertNotEquals(-1, car2Exit);
        assertTrue(bus2Exit < car2Exit);
    }

    @Test
    void partialConfigure_unspecifiedFieldsShouldNotResetToDefault() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input_partial_configure.json");
        List<StepStatus> steps = engine.runSimulation().getStepStatuses();

        int bus1Exit = findExitStep(steps, "bus1");
        int car1Exit = findExitStep(steps, "car1");

        assertNotEquals(-1, bus1Exit);
        assertNotEquals(-1, car1Exit);
        assertTrue(bus1Exit <= car1Exit);
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
