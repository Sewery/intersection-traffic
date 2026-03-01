package com.seweryn.tasior.statistics;

import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.Turn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsCollectorTest {

    private StatisticsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new StatisticsCollector();
    }

    @Test
    void shouldCalculateAverageWaitTime() {
        collector.onEvent(new SimulationEvent.VehicleArrived("car1", Direction.NORTH, 0));
        collector.onEvent(new SimulationEvent.VehicleArrived("car2", Direction.NORTH, 0));
        collector.onEvent(new SimulationEvent.VehicleExited("car1", 2));  // wait=2
        collector.onEvent(new SimulationEvent.VehicleExited("car2", 4));  // wait=4

        Statistics stats = collector.compute();

        assertEquals(3.0, stats.averageWaitTime());
    }

    @Test
    void shouldCalculatePercentiles() {
        for (int i = 1; i <= 10; i++) {
            collector.onEvent(new SimulationEvent.VehicleArrived("car" + i, Direction.NORTH, 0));
            collector.onEvent(new SimulationEvent.VehicleExited("car" + i, i));
        }

        Statistics stats = collector.compute();

        assertEquals(5.0,  stats.percentiles().p50());
        assertEquals(8.0,  stats.percentiles().p75());
        assertEquals(9.0, stats.percentiles().p90());
    }

    @Test
    void shouldCountStuckVehicles() {
        collector.onEvent(new SimulationEvent.VehicleArrived("car1", Direction.NORTH, 0));
        collector.onEvent(new SimulationEvent.VehicleStuck("car1", Direction.NORTH));

        Statistics stats = collector.compute();

        assertEquals(1, stats.totalVehiclesStuck());
        assertEquals(0, stats.totalVehiclesProcessed());
    }

    @Test
    void shouldTrackBlockedLaneFromAndToStep() {
        collector.onEvent(new SimulationEvent.LaneBlocked(Direction.NORTH, Turn.STRAIGHT, 2, 3));
        collector.onEvent(new SimulationEvent.LaneUnblocked(Direction.NORTH, Turn.STRAIGHT, 7));

        Statistics stats = collector.compute();

        assertEquals(1, stats.blockedLanes().size());
        assertEquals(3, stats.blockedLanes().get(0).blockedFromStep());
        assertEquals(7, stats.blockedLanes().get(0).blockedToStep());
    }

    @Test
    void shouldMarkLaneAsStillBlockedWhenNotUnblocked() {
        collector.onEvent(new SimulationEvent.LaneBlocked(Direction.EAST, Turn.RIGHT, 1, 5));

        Statistics stats = collector.compute();

        assertEquals(-1, stats.blockedLanes().get(0).blockedToStep());
    }

    @Test
    void shouldCalculatePerRoadStats() {
        collector.onEvent(new SimulationEvent.VehicleArrived("n1", Direction.NORTH, 0));
        collector.onEvent(new SimulationEvent.VehicleArrived("n2", Direction.NORTH, 0));
        collector.onEvent(new SimulationEvent.VehicleArrived("e1", Direction.EAST,  0));
        collector.onEvent(new SimulationEvent.VehicleExited("n1", 2));
        collector.onEvent(new SimulationEvent.VehicleExited("n2", 4));
        collector.onEvent(new SimulationEvent.VehicleExited("e1", 1));

        Statistics stats = collector.compute();

        assertEquals(3.0, stats.perRoadStats().get(Direction.NORTH).avgWaitTime());
        assertEquals(2,   stats.perRoadStats().get(Direction.NORTH).vehiclesProcessed());
        assertEquals(1.0, stats.perRoadStats().get(Direction.EAST).avgWaitTime());
    }

    @Test
    void shouldReturnZeroAverageWaitTimeWhenNoVehiclesExited() {
        collector.onEvent(new SimulationEvent.VehicleArrived("car1", Direction.NORTH, 0));

        assertEquals(0.0, collector.compute().averageWaitTime());
    }

    @Test
    void shouldReturnZeroPercentilesWhenNoVehiclesExited() {
        Statistics.Percentiles p = collector.compute().percentiles();

        assertAll(
                () -> assertEquals(0, p.p50()),
                () -> assertEquals(0, p.p75()),
                () -> assertEquals(0, p.p90())
        );
    }

    @Test
    void shouldCountVehiclesStuckPerRoad() {
        collector.onEvent(new SimulationEvent.VehicleArrived("n1", Direction.NORTH, 0));
        collector.onEvent(new SimulationEvent.VehicleArrived("n2", Direction.NORTH, 0));
        collector.onEvent(new SimulationEvent.VehicleStuck("n1", Direction.NORTH));

        Statistics stats = collector.compute();

        assertEquals(1, stats.perRoadStats().get(Direction.NORTH).vehiclesStuck());
    }

    @Test
    void shouldHandleMultipleBlocksOnSameLane() {
        collector.onEvent(new SimulationEvent.LaneBlocked(Direction.NORTH, Turn.STRAIGHT, 1, 2));
        collector.onEvent(new SimulationEvent.LaneUnblocked(Direction.NORTH, Turn.STRAIGHT, 5));
        collector.onEvent(new SimulationEvent.LaneBlocked(Direction.NORTH, Turn.STRAIGHT, 0, 8));

        Statistics stats = collector.compute();

        assertEquals(2, stats.blockedLanes().size());
    }
}
