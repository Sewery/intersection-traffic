package com.seweryn.tasior.model;

public class TrafficLight {

    public enum State {
        GREEN,
        YELLOW_TO_RED,
        YELLOW_TO_GREEN,
        RED
    }

    private State state;
    private int yellowStepsRemaining;
    public static final int YELLOW_TIME = 3;

    public TrafficLight(State initialState) {
        this.state = initialState;
        this.yellowStepsRemaining = 0;
    }

    public void startTransitionToRed() {
        this.state = State.YELLOW_TO_RED;
        this.yellowStepsRemaining = YELLOW_TIME;
    }

    public void startTransitionToGreen() {
        this.state = State.YELLOW_TO_GREEN;
        this.yellowStepsRemaining = YELLOW_TIME;
    }

    public void tick() {
        if (state == State.YELLOW_TO_RED || state == State.YELLOW_TO_GREEN) {
            yellowStepsRemaining--;
            if (yellowStepsRemaining <= 0) {
                state = state == State.YELLOW_TO_RED ? State.RED : State.GREEN;
            }
        }
    }

    public boolean isInTransition() {
        return state == State.YELLOW_TO_RED || state == State.YELLOW_TO_GREEN;
    }

    public boolean isGreen() {
        return state == State.GREEN;
    }

    public void setGreen() {
        this.state = State.GREEN;
    }

    public State getState() {
        return state;
    }
}
