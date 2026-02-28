package com.seweryn.tasior.model;

public class Vehicle {
    private final String vehicleId;
    private final int arrivalStep;
    private VehicleState state;

    public Vehicle(String vehicleId, int arrivalStep) {
        this.vehicleId = vehicleId;
        this.arrivalStep = arrivalStep;
        this.state = VehicleState.WAITING;
    }

    public String vehicleId()    { return vehicleId; }
    public int arrivalTime()     { return arrivalStep; }
    public VehicleState getState() { return state; }
    public void setState(VehicleState state) { this.state = state; }

    public int waitTime(int currentStep) {
        return currentStep - arrivalStep;
    }
}