package com.seweryn.tasior.model;

import java.util.*;

public class Lane {
    private final Turn allowedTurn;
    private final Queue<Vehicle> waitingVehicles = new LinkedList<>();
    private final TrafficLight trafficLight = new TrafficLight(TrafficLight.State.RED);
    private Vehicle crossingVehicle = null;
    private boolean blocked = false;

    public Lane(Turn allowedTurn) {
        this.allowedTurn = allowedTurn;
    }

    public void addVehicle(Vehicle vehicle) {
        waitingVehicles.add(vehicle);
    }

    public Optional<Vehicle> pollVehicle() {
        return Optional.ofNullable(waitingVehicles.poll());
    }

    public void startCrossing() {
        crossingVehicle = waitingVehicles.poll();
        if (crossingVehicle != null) {
            crossingVehicle.setState(VehicleState.CROSSING);
        }
    }

    public Optional<Vehicle> finishCrossing() {
        if (crossingVehicle == null) return Optional.empty();
        crossingVehicle.setState(VehicleState.EXITED);
        Vehicle exited = crossingVehicle;
        crossingVehicle = null;
        return Optional.of(exited);
    }

    public boolean isCrossing() {
        return crossingVehicle != null;
    }

    public TrafficLight getTrafficLight(){
        return trafficLight;
    }

    public Turn getAllowedTurn(){
        return allowedTurn;
    }

    public int getVehicleCount(){
        return waitingVehicles.size();
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setYellowTime(int yellowTime) {
        trafficLight.setYellowTime(yellowTime);
    }

    public boolean isPassable(){
        return trafficLight.isGreen() && !blocked;
    }

    public Collection<Vehicle> getWaitingVehicles(){
        return Collections.unmodifiableCollection(waitingVehicles);
    }

    public boolean isEmpty() {
        return waitingVehicles.isEmpty();
    }
}
