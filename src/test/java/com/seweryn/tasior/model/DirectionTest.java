package com.seweryn.tasior.model;


import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.Turn;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DirectionTest {

    @Test
    void northToSouthShouldBeStraight() {
        assertEquals(Turn.STRAIGHT, Direction.findTurn(Direction.NORTH, Direction.SOUTH));
    }

    @Test
    void northToEastShouldBeRight() {
        assertEquals(Turn.RIGHT, Direction.findTurn(Direction.NORTH, Direction.EAST));
    }

    @Test
    void northToWestShouldBeLeft() {
        assertEquals(Turn.LEFT, Direction.findTurn(Direction.NORTH, Direction.WEST));
    }

    @Test
    void southToNorthShouldBeStraight() {
        assertEquals(Turn.STRAIGHT, Direction.findTurn(Direction.SOUTH, Direction.NORTH));
    }

    @Test
    void southToWestShouldBeRight() {
        assertEquals(Turn.RIGHT, Direction.findTurn(Direction.SOUTH, Direction.WEST));
    }

    @Test
    void southToEastShouldBeLeft() {
        assertEquals(Turn.LEFT, Direction.findTurn(Direction.SOUTH, Direction.EAST));
    }

    @Test
    void eastToWestShouldBeStraight() {
        assertEquals(Turn.STRAIGHT, Direction.findTurn(Direction.EAST, Direction.WEST));
    }

    @Test
    void westToEastShouldBeStraight() {
        assertEquals(Turn.STRAIGHT, Direction.findTurn(Direction.WEST, Direction.EAST));
    }

    @Test
    void fromStringShouldBeCaseInsensitive() {
        assertEquals(Direction.NORTH, Direction.fromString("north"));
        assertEquals(Direction.NORTH, Direction.fromString("NORTH"));
        assertEquals(Direction.NORTH, Direction.fromString("North"));
    }

    @Test
    void fromStringInvalidShouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> Direction.fromString("invalid"));
    }
}
