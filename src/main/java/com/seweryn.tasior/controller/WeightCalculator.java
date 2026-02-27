package com.seweryn.tasior.controller;

import com.seweryn.tasior.model.Intersection;
import com.seweryn.tasior.model.Lane;
import com.seweryn.tasior.model.Movement;
import com.seweryn.tasior.model.Vehicle;

import java.util.Set;

public class WeightCalculator {

    private double busPriority;
    private double carPriority;

    public WeightCalculator() {
        this.carPriority = 1.0;
        this.busPriority = 5.0;
    }

    public WeightCalculator(double carPriority, double busPriority) {
        this.carPriority = carPriority;
        this.busPriority = busPriority;
    }

    public void configure(double carPriority, double busPriority) {
        this.carPriority = carPriority;
        this.busPriority = busPriority;
    }

    public double calculatePhaseWeight(Set<Movement> phase, Intersection intersection, int currentStep) {
        return phase.stream()
                .mapToDouble(m -> calculateLaneWeight(
                        intersection.getRoad(m.from()).getLaneByTurn(m.turn()),
                        currentStep
                ))
                .sum();
    }

    private double calculateLaneWeight(Lane lane, int currentStep) {
        return lane.getWaitingVehicles().stream()
                .mapToDouble(v -> Math.pow(currentStep - v.arrivalTime(), 1.5)
                        * getPriority(v))
                .sum();
    }

    private double getPriority(Vehicle v) {
        return v.vehicleId().startsWith("bus") ? busPriority : carPriority;
    }
}
