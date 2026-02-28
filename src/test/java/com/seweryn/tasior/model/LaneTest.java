package com.seweryn.tasior.model;

import com.seweryn.tasior.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LaneTest {

    private Lane lane;

    @BeforeEach
    void setUp() {
        lane = new Lane(Turn.STRAIGHT);
    }

    // stan początkowy

    @Test
    void newLaneShouldBeEmpty() {
        assertTrue(lane.isEmpty());
        assertEquals(0, lane.getVehicleCount());
    }

    @Test
    void newLaneShouldHaveRedLight() {
        assertEquals(TrafficLight.State.RED, lane.getTrafficLight().getState());
    }

    @Test
    void newLaneShouldNotBeCrossing() {
        assertFalse(lane.isCrossing());
    }

    @Test
    void newLaneShouldNotBePassable() {
        assertFalse(lane.isPassable());
    }

    // dodawanie pojazdów

    @Test
    void addVehicleShouldIncreaseCount() {
        lane.addVehicle(new Vehicle("car1", 0));
        assertEquals(1, lane.getVehicleCount());
        assertFalse(lane.isEmpty());
    }

    @Test
    void addMultipleVehiclesShouldIncreaseCount() {
        lane.addVehicle(new Vehicle("car1", 0));
        lane.addVehicle(new Vehicle("car2", 1));
        lane.addVehicle(new Vehicle("car3", 2));
        assertEquals(3, lane.getVehicleCount());
    }

    // pollVehicle

    @Test
    void pollShouldReturnFirstVehicle() {
        Vehicle car1 = new Vehicle("car1", 0);
        Vehicle car2 = new Vehicle("car2", 1);
        lane.addVehicle(car1);
        lane.addVehicle(car2);

        assertEquals(Optional.of(car1), lane.pollVehicle());
        assertEquals(1, lane.getVehicleCount());
    }

    @Test
    void pollOnEmptyLaneShouldReturnEmpty() {
        assertEquals(Optional.empty(), lane.pollVehicle());
    }

    @Test
    void getWaitingVehiclesShouldBeUnmodifiable() {
        lane.addVehicle(new Vehicle("car1", 0));
        assertThrows(UnsupportedOperationException.class,
                () -> lane.getWaitingVehicles().clear());
    }

    // startCrossing / finishCrossing

    @Test
    void startCrossingShouldSetVehicleStateToCrossing() {
        Vehicle car1 = new Vehicle("car1", 0);
        lane.addVehicle(car1);
        lane.startCrossing();

        assertTrue(lane.isCrossing());
        assertEquals(VehicleState.CROSSING, car1.getState());
    }

    @Test
    void startCrossingShouldRemoveVehicleFromQueue() {
        lane.addVehicle(new Vehicle("car1", 0));
        lane.startCrossing();

        assertTrue(lane.isEmpty(),
                "Pojazd powinien opuścić kolejkę po startCrossing");
    }

    @Test
    void startCrossingOnEmptyLaneShouldNotBeCrossing() {
        lane.startCrossing();
        assertFalse(lane.isCrossing());
    }

    @Test
    void finishCrossingShouldReturnVehicle() {
        Vehicle car1 = new Vehicle("car1", 0);
        lane.addVehicle(car1);
        lane.startCrossing();

        Optional<Vehicle> exited = lane.finishCrossing();

        assertTrue(exited.isPresent());
        assertEquals("car1", exited.get().vehicleId());
    }

    @Test
    void finishCrossingShouldSetVehicleStateToExited() {
        Vehicle car1 = new Vehicle("car1", 0);
        lane.addVehicle(car1);
        lane.startCrossing();
        lane.finishCrossing();

        assertEquals(VehicleState.EXITED, car1.getState());
    }

    @Test
    void finishCrossingShouldClearCrossingVehicle() {
        lane.addVehicle(new Vehicle("car1", 0));
        lane.startCrossing();
        lane.finishCrossing();

        assertFalse(lane.isCrossing());
    }

    @Test
    void finishCrossingOnEmptyLaneShouldReturnEmpty() {
        assertEquals(Optional.empty(), lane.finishCrossing());
    }

    @Test
    void secondVehicleShouldWaitWhileFirstIsCrossing() {
        lane.addVehicle(new Vehicle("car1", 0));
        lane.addVehicle(new Vehicle("car2", 0));
        lane.startCrossing();

        assertEquals(1, lane.getVehicleCount(),
                "car2 powinien nadal czekać w kolejce");
    }

    // czasy oczekiwania

    @Test
    void averageWaitTimeShouldBeCorrect() {
        lane.addVehicle(new Vehicle("car1", 0));
        lane.addVehicle(new Vehicle("car2", 2));

        // step=4: car1 czeka 4, car2 czeka 2 → avg = 3.0
        assertEquals(3.0, lane.getAverageWaitTime(4), 0.001);
    }

    @Test
    void averageWaitTimeOnEmptyLaneShouldBeZero() {
        assertEquals(0.0, lane.getAverageWaitTime(5));
    }

    @Test
    void percentileWaitTimeShouldBeCorrect() {
        lane.addVehicle(new Vehicle("car1", 0));   // czeka 10
        lane.addVehicle(new Vehicle("car2", 5));   // czeka 5

        // sorted: [5, 10], p50 → rank = ceil(0.5*2)-1 = 0 → 5
        assertEquals(5, lane.getPercentileWaitTime(10, 50));
    }

    @Test
    void percentile100ShouldReturnMaxWaitTime() {
        lane.addVehicle(new Vehicle("car1", 0));   // czeka 10
        lane.addVehicle(new Vehicle("car2", 5));   // czeka 5

        // sorted: [5, 10], p100 → rank = ceil(1.0*2)-1 = 1 → 10
        assertEquals(10, lane.getPercentileWaitTime(10, 100));
    }

    @Test
    void percentileOnEmptyLaneShouldReturnZero() {
        assertEquals(0, lane.getPercentileWaitTime(5, 50));
    }
}