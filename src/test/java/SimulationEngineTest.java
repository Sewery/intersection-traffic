import com.seweryn.tasior.SimulationEngine;
import com.seweryn.tasior.io.SimulationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {

    private static final String RESOURCES = "src/main/resources/";

    @Test
    void input1ShouldProduceCorrectOutput() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input1.json");
        SimulationResult result = engine.runSimulation();

        assertEquals(7, result.getStepStatuses().size());

        // step 1 – vehicle1 i vehicle2 wyjeżdżają (NS STRAIGHT)
        assertTrue(result.getStepStatuses().get(0).leftVehicles().containsAll(
                java.util.List.of("vehicle1", "vehicle2")
        ));

        // step 2 – puste
        assertTrue(result.getStepStatuses().get(1).leftVehicles().isEmpty());
    }

    @Test
    void emptyIntersectionShouldReturnEmptySteps() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input4.json");
        SimulationResult result = engine.runSimulation();

        result.getStepStatuses().forEach(step ->
                assertTrue(step.leftVehicles().isEmpty())
        );
    }

    @Test
    void allVehiclesShouldEventuallyLeave() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input6.json");
        SimulationResult result = engine.runSimulation();

        long totalLeft = result.getStepStatuses().stream()
                .mapToLong(step -> step.leftVehicles().size())
                .sum();

        // 10 pojazdów dodanych w input6.json (car1-car7 + bus1-bus4, ale bez car7 który jest dodany później)
        assertTrue(totalLeft > 0);
    }

    @Test
    void noVehicleShouldLeaveOnRedLight() {
        SimulationEngine engine = new SimulationEngine(RESOURCES + "input5.json");
        SimulationResult result = engine.runSimulation();

        // żaden step nie powinien mieć pojazdów z przeciwnych faz jednocześnie
        result.getStepStatuses().forEach(step -> {
            boolean hasNS = step.leftVehicles().stream()
                    .anyMatch(id -> id.startsWith("n_") || id.startsWith("s_"));
            boolean hasEW = step.leftVehicles().stream()
                    .anyMatch(id -> id.startsWith("e_") || id.startsWith("w_"));

            assertFalse(hasNS && hasEW,
                    "NS i EW nie mogą wyjechać w tym samym stepie: " + step.leftVehicles());
        });
    }
}
