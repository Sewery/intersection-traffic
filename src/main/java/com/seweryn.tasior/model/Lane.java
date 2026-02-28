package com.seweryn.tasior.model;

import java.util.*;

public class Lane {
    private final Turn allowedTurn;
    private final Queue<Vehicle> waitingVehicles = new LinkedList<>();
    private final TrafficLight trafficLight = new TrafficLight(TrafficLight.State.RED);
    private Vehicle crossingVehicle = null;

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

    public double getAverageWaitTime(int currentStep) {
        return waitingVehicles.stream()
                .mapToInt(v -> currentStep - v.arrivalTime())
                .average()
                .orElse(0.0);
    }

    public int getPercentileWaitTime(int currentStep, double percentile) {
        List<Integer> waitTimes = waitingVehicles.stream()
                .map(v -> currentStep - v.arrivalTime())
                .sorted()
                .toList();

        if (waitTimes.isEmpty()) return 0;
        int rank = (int) Math.ceil(percentile / 100.0 * waitTimes.size()) - 1;
        return waitTimes.get(rank);
    }

    public TrafficLight getTrafficLight(){
        return trafficLight;
    }

    public Turn getAllowedTurn(){
        return allowedTurn;
    }

    public boolean isEmpty(){
        return waitingVehicles.isEmpty();
    }

    public int getVehicleCount(){
        return waitingVehicles.size();
    }

    public boolean isPassable(){
        return trafficLight.isGreen();
    }

    public Collection<Vehicle> getWaitingVehicles(){
        return Collections.unmodifiableCollection(waitingVehicles);
    }
}
