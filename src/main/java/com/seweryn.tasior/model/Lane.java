package com.seweryn.tasior.model;

import java.util.*;

public class Lane {
    private final Turn allowedTurn;
    private boolean isOpen;
    private final Queue<Vehicle> waitingVehicles = new LinkedList<>();
    private final TrafficLight trafficLight = new TrafficLight(TrafficLight.State.RED);

    public Lane(Turn allowedTurn) {
        this.allowedTurn = allowedTurn; // Poprawione
        this.isOpen = false;
    }

    public void addVehicle(Vehicle vehicle) {
        waitingVehicles.add(vehicle);  // arrivalTime jest w Vehicle, nie trzeba osobnej listy
    }

    public Vehicle pollVehicle() {
        return waitingVehicles.poll();
    }

    // zamiast getSumOfVehiclesTimesOfWaiting - oblicz na bieżąco z Vehicle
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

    public Turn getAllowedTurn() {
        return allowedTurn;
    }

    public boolean isEmpty() {
        return waitingVehicles.isEmpty();
    }

    public int getVehicleCount(){
        return waitingVehicles.size();
    }

    public Collection<Vehicle> getWaitingVehicles(){
        return Collections.unmodifiableCollection(waitingVehicles);
    }
}
