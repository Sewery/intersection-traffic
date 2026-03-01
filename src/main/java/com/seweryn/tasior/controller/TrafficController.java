package com.seweryn.tasior.controller;

import com.seweryn.tasior.model.Intersection;
import com.seweryn.tasior.model.Movement;
import com.seweryn.tasior.model.TrafficLight;

import java.util.Comparator;
import java.util.Set;

public class TrafficController{
    private final Intersection intersection;
    private final WeightCalculator weightCalculator;
    private Set<Movement> currentPhase;
    private Set<Movement> nextPhase;

    public TrafficController(Intersection intersection, WeightCalculator weightCalculator) {
        this.intersection = intersection;
        this.weightCalculator = weightCalculator;
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

        // znajdź fazę która obsługuje zagłodzone pojazdy
        return TrafficCompatibility.getPhaseGroups().stream()
                .filter(phase -> phase.stream().anyMatch(starvingMovements::contains))
                .max(Comparator.comparingDouble(
                        phase -> weightCalculator.calculatePhaseWeight(phase, intersection, stepCounter)
                ))
                .orElseGet(() -> findBestPhase(stepCounter));  // fallback
    }

    private Set<Movement> findBestPhase(int stepCounter) {
        return TrafficCompatibility.getPhaseGroups().stream()
                .max(Comparator.comparingDouble(
                        phase -> weightCalculator.calculatePhaseWeight(phase, intersection, stepCounter)
                ))
                .orElse(currentPhase);
    }

    private boolean shouldSwitch(Set<Movement> bestPhase, int stepCounter) {
        if (currentPhase == null) return true;
        if (currentPhase.equals(bestPhase)) return false;

        double currentWeight = weightCalculator.calculatePhaseWeight(currentPhase, intersection, stepCounter);
        double bestWeight = weightCalculator.calculatePhaseWeight(bestPhase, intersection, stepCounter);

        double switchingPenalty = currentWeight * TrafficLight.YELLOW_TIME;
        return (bestWeight - currentWeight) > switchingPenalty;
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