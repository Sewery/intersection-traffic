package com.seweryn.tasior.model;

public enum Direction {
    NORTH, EAST, SOUTH, WEST;

    public static Direction fromString(String direction){
        return switch(direction.toLowerCase()){
            case "north" -> NORTH;
            case "south" -> SOUTH;
            case "east" -> EAST;
            case "west" -> WEST;
            default -> throw new IllegalArgumentException("Invalid direction");
        };
    }

    public static Turn findTurn(Direction start, Direction end) {
        int diff = (end.ordinal() - start.ordinal() + 4) % 4;
        return switch (diff) {
            case 1 -> Turn.RIGHT;
            case 2 -> Turn.STRAIGHT;
            case 3 -> Turn.LEFT;
            default -> throw new IllegalArgumentException("Same direction: " + start);
        };
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
