package com.seweryn.tasior.model;

public class Vehicle {
    private final String vehicleId;
    private final int arrivalStep;
    private final VehicleType type;
    private VehicleState state;

    public Vehicle(String vehicleId, int arrivalStep, VehicleType type) {
        this.vehicleId = vehicleId;
        this.arrivalStep = arrivalStep;
        this.type = type;
        this.state = VehicleState.WAITING;
    }

    public Vehicle(String vehicleId, int arrivalStep) {
        this(vehicleId, arrivalStep, VehicleType.CAR);
    }

    public String vehicleId(){
        return vehicleId;
    }

    public VehicleState getState(){
        return state;
    }

    public VehicleType type() {
        return type;
    }

    public void setState(VehicleState state){
        this.state = state;
    }

    public int waitTime(int currentStep) {
        return currentStep - arrivalStep;
    }
}