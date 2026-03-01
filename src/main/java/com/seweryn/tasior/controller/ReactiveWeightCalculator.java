package com.seweryn.tasior.controller;

import com.seweryn.tasior.model.Intersection;
import com.seweryn.tasior.model.Lane;
import com.seweryn.tasior.model.Movement;
import com.seweryn.tasior.model.Vehicle;

import java.util.Set;
import java.util.stream.Collectors;

public class ReactiveWeightCalculator implements WeightCalculator{
    private double carPriority = TrafficDefaults.CAR_PRIORITY;
    private double busPriority = TrafficDefaults.BUS_PRIORITY;
    private int maxWaitTime = TrafficDefaults.MAX_WAIT_TIME;

    @Override
    public void configure(double carPriority, double busPriority) {
        this.carPriority = carPriority;
        this.busPriority = busPriority;
    }

    @Override
    public void configureTimings(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    @Override
    public double calculatePhaseWeight(Set<Movement> phase, Intersection intersection, int currentStep) {
        return phase.stream()
                .mapToDouble(m -> intersection.getRoad(m.from())
                        .getLaneByTurn(m.turn())
                        .map(lane -> calculateLaneWeight(lane, currentStep))
                        .orElse(0.0)
                )
                .sum();
    }

    private double calculateLaneWeight(Lane lane, int currentStep) {
        if (lane.isEmpty()) return 0.0;

        return lane.getWaitingVehicles().stream()
                .mapToDouble(v -> {
                    double waitBonus = v.waitTime(currentStep) >= maxWaitTime
                            ? 1_000_000.0
                            : 1.0 + Math.pow(v.waitTime(currentStep), 1.5);
                    return waitBonus * getPriority(v);
                })
                .sum();
    }

    private double getPriority(Vehicle v) {
        return switch (v.type()) {
            case BUS -> busPriority;
            case CAR -> carPriority;
        };
    }

    @Override
    public boolean hasStarvingVehicle(Intersection intersection, int currentStep) {
        return intersection.getRoads().stream()
                .flatMap(road -> road.getLanes().stream())
                .flatMap(lane -> lane.getWaitingVehicles().stream())
                .anyMatch(v -> v.waitTime(currentStep) >= maxWaitTime);
    }

    @Override
    public Set<Movement> getStarvingMovements(Intersection intersection, int currentStep) {
        return intersection.getRoads().stream()
                .flatMap(road -> road.getLanes().stream())
                .filter(lane -> lane.getWaitingVehicles().stream()
                        .anyMatch(v -> v.waitTime(currentStep) >= maxWaitTime))
                .map(lane -> new Movement(
                        intersection.getRoadByLane(lane).getLocation(),
                        lane.getAllowedTurn()))
                .collect(Collectors.toSet());
    }
}
