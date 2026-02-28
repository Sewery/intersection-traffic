package model;

import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.Road;
import com.seweryn.tasior.model.Turn;
import com.seweryn.tasior.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoadTest {

    private Road road;

    @BeforeEach
    void setUp() {
        road = Road.createThreeLineRoad(Direction.NORTH);
    }

    @Test
    void northToSouthShouldBeStraight() {
        road.addVehicleToLane(Direction.SOUTH, new Vehicle("car1", 0));
        assertEquals(1, road.getLaneByTurn(Turn.STRAIGHT).orElseThrow().getVehicleCount());
    }

    @Test
    void northToEastShouldBeRight() {
        road.addVehicleToLane(Direction.EAST, new Vehicle("car1", 0));
        assertEquals(1, road.getLaneByTurn(Turn.RIGHT).orElseThrow().getVehicleCount());
    }

    @Test
    void northToWestShouldBeLeft() {
        road.addVehicleToLane(Direction.WEST, new Vehicle("car1", 0));
        assertEquals(1, road.getLaneByTurn(Turn.LEFT).orElseThrow().getVehicleCount());
    }

    @Test
    void addVehicleShouldNotAffectOtherLanes() {
        road.addVehicleToLane(Direction.SOUTH, new Vehicle("car1", 0));

        assertAll(
            () -> assertEquals(0, road.getLaneByTurn(Turn.LEFT).orElseThrow().getVehicleCount()),
            () -> assertEquals(0, road.getLaneByTurn(Turn.RIGHT).orElseThrow().getVehicleCount())
        );
    }
}