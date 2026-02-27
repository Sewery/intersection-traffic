package model;

import com.seweryn.tasior.model.Lane;
import com.seweryn.tasior.model.TrafficLight;
import com.seweryn.tasior.model.Turn;
import com.seweryn.tasior.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LaneTest {

    private Lane lane;

    @BeforeEach
    void setUp() {
        lane = new Lane(Turn.STRAIGHT);
    }

    @Test
    void newLaneShouldBeEmpty() {
        assertTrue(lane.isEmpty());
        assertEquals(0, lane.getVehicleCount());
    }

    @Test
    void addVehicleShouldIncreaseCount() {
        lane.addVehicle(new Vehicle(0, "car1"));
        assertEquals(1, lane.getVehicleCount());
        assertFalse(lane.isEmpty());
    }

    @Test
    void pollShouldReturnFirstVehicle() {
        Vehicle car1 = new Vehicle(0, "car1");
        Vehicle car2 = new Vehicle(1, "car2");
        lane.addVehicle(car1);
        lane.addVehicle(car2);

        assertEquals(car1, lane.pollVehicle());
        assertEquals(1, lane.getVehicleCount());
    }

    @Test
    void getWaitingVehiclesShouldBeUnmodifiable() {
        lane.addVehicle(new Vehicle(0, "car1"));
        assertThrows(UnsupportedOperationException.class,
                () -> lane.getWaitingVehicles().clear());
    }

    @Test
    void newLaneShouldHaveRedLight() {
        assertEquals(TrafficLight.State.RED, lane.getTrafficLight().getState());
    }

    @Test
    void averageWaitTimeShouldBeCorrect() {
        lane.addVehicle(new Vehicle(0, "car1"));
        lane.addVehicle(new Vehicle(2, "car2"));

        // step=4: car1 czeka 4, car2 czeka 2 → avg = 3.0
        assertEquals(3.0, lane.getAverageWaitTime(4), 0.001);
    }

    @Test
    void averageWaitTimeEmptyLaneShouldBeZero() {
        assertEquals(0.0, lane.getAverageWaitTime(5));
    }

    @Test
    void percentileWaitTimeShouldBeCorrect() {
        lane.addVehicle(new Vehicle(0, "car1")); // czeka 10
        lane.addVehicle(new Vehicle(5, "car2")); // czeka 5

        // p50 przy step=10: [5, 10] → rank=1 → 10? nie, sorted: [5,10], rank=ceil(0.5*2)-1=0 → 5
        assertEquals(5, lane.getPercentileWaitTime(10, 50));
    }
}