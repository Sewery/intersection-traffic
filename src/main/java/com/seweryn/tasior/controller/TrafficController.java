package com.seweryn.tasior.controller;

import com.seweryn.tasior.model.Intersection;
import com.seweryn.tasior.model.Movement;
import com.seweryn.tasior.model.TrafficLight;

import java.util.Comparator;
import java.util.Set;

public class TrafficController{
    private final Intersection intersection;
    private WeightCalculator weightCalculator;
    private Set<Movement> currentPhase;
    private Set<Movement> nextPhase;
    private int yellowTime = TrafficDefaults.YELLOW_TIME;

    public TrafficController(Intersection intersection, WeightCalculator weightCalculator) {
        this.intersection = intersection;
        this.weightCalculator = weightCalculator;
    }

    public void setWeightCalculator(WeightCalculator weightCalculator) {
        this.weightCalculator = weightCalculator;
    }

    public void configureTimings(int maxWaitTime, int yellowTime) {
        this.yellowTime = yellowTime;
        weightCalculator.configureTimings(maxWaitTime);
        intersection.getRoads().forEach(road ->
                road.getLanes().forEach(lane -> lane.setYellowTime(yellowTime))
        );
    }

    public void executeStep(int stepCounter) {
        if (isInTransition()) {
            tickTransition();
            return;
        }

        Set<Movement> targetPhase = weightCalculator.hasStarvingVehicle(intersection, stepCounter)
                ? findStarvingPhase(stepCounter)
                : findBestPhase(stepCounter);

        if (currentPhase == null || shouldSwitch(targetPhase, stepCounter)) {
            initiateTransition(targetPhase);
        }
    }

    private Set<Movement> findStarvingPhase(int stepCounter) {
        Set<Movement> starvingMovements =
                weightCalculator.getStarvingMovements(intersection, stepCounter);

        // Find a phase with starving vehicles
        return TrafficCompatibility.getPhaseGroups().stream()
                .filter(phase -> phase.stream().anyMatch(starvingMovements::contains))
                .max(Comparator.comparingDouble(
                        phase -> weightCalculator.calculatePhaseWeight(phase, intersection, stepCounter)
                ))
                .orElseGet(() -> findBestPhase(stepCounter));
    }

    private Set<Movement> findBestPhase(int stepCounter) {
        return TrafficCompatibility.getPhaseGroups().stream()
                .max(Comparator.comparingDouble(
                        phase -> weightCalculator.calculatePhaseWeight(phase, intersection, stepCounter)
                ))
                .orElse(currentPhase);
    }

    private boolean shouldSwitch(Set<Movement> targetPhase, int stepCounter) {
        if (currentPhase == null) return true;
        if (currentPhase.equals(targetPhase)) return false;
        if (isStarvingSwitch(targetPhase, stepCounter)) return true;

        double currentWeight    = weightCalculator.calculatePhaseWeight(currentPhase, intersection, stepCounter);
        double targetWeight     = weightCalculator.calculatePhaseWeight(targetPhase,  intersection, stepCounter);
        double switchingPenalty = currentWeight * yellowTime;

        return (targetWeight - currentWeight) > switchingPenalty;
    }

    private boolean isStarvingSwitch(Set<Movement> targetPhase, int stepCounter) {
        Set<Movement> starving = weightCalculator.getStarvingMovements(intersection, stepCounter);
        return targetPhase.stream().anyMatch(starving::contains);
    }

    private void initiateTransition(Set<Movement> bestPhase) {
        this.nextPhase = bestPhase;
        intersection.getRoads().forEach(road ->
                road.getLanes().forEach(lane -> {
                    Movement m = new Movement(road.getLocation(), lane.getAllowedTurn());
                    if (currentPhase == null) {
                        // pierwsza faza - od razu GREEN bez żółtego
                        if (nextPhase.contains(m)) {
                            lane.getTrafficLight().setGreen();
                        }
                    } else {
                        if (currentPhase.contains(m)) {
                            lane.getTrafficLight().startTransitionToRed();
                        } else if (nextPhase.contains(m)) {
                            lane.getTrafficLight().startTransitionToGreen();
                        }
                    }
                })
        );
        if (currentPhase == null) {
            currentPhase = nextPhase;
            nextPhase = null;
        }
    }
    private void tickTransition() {
        intersection.getRoads().forEach(road ->
                road.getLanes().forEach(lane -> lane.getTrafficLight().tick())
        );
        if (!isInTransition()) {
            currentPhase = nextPhase;
            nextPhase = null;
        }
    }

    private boolean isInTransition() {
        return intersection.getRoads().stream()
                .flatMap(road -> road.getLanes().stream())
                .anyMatch(lane -> lane.getTrafficLight().isInTransition());
    }

    public Set<Movement> getCurrentPhase(){
        return currentPhase;
    }
}