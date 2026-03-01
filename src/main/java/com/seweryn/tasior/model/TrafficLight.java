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
    private int yellowTime;

    public TrafficLight(State initialState) {
        this(initialState, com.seweryn.tasior.controller.TrafficDefaults.YELLOW_TIME);
    }

    public TrafficLight(State initialState, int yellowTime) {
        this.state = initialState;
        this.yellowTime = yellowTime;
        this.yellowStepsRemaining = 0;
    }

    public void setYellowTime(int yellowTime) {
        this.yellowTime = yellowTime;
    }

    public void startTransitionToRed() {
        this.state = State.YELLOW_TO_RED;
        this.yellowStepsRemaining = yellowTime;
    }

    public void startTransitionToGreen() {
        this.state = State.YELLOW_TO_GREEN;
        this.yellowStepsRemaining = yellowTime;
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
