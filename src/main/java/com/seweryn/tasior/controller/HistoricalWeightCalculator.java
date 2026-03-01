package com.seweryn.tasior.controller;

import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.Intersection;
import com.seweryn.tasior.model.Movement;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HistoricalWeightCalculator implements WeightCalculator {

    private final Map<Direction, List<TimeSlot>> historicalData;
    private final ReactiveWeightCalculator reactive;

    public HistoricalWeightCalculator(Map<Direction, List<TimeSlot>> historicalData) {
        this.historicalData = historicalData;
        this.reactive = new ReactiveWeightCalculator();
    }

    @Override
    public void configure(double carPriority, double busPriority) {
        reactive.configure(carPriority, busPriority);
    }

    @Override
    public double calculatePhaseWeight(Set<Movement> phase, Intersection intersection, int currentStep) {
        return phase.stream()
                .mapToDouble(m -> {
                    double reactiveWeight = reactive.calculatePhaseWeight(
                            Set.of(m), intersection, currentStep);

                    // starvation nadpisuje historical factor
                    boolean hasStarving = reactive.getStarvingMovements(intersection, currentStep)
                            .contains(m);
                    if (hasStarving) return reactiveWeight;

                    double factor = getFactor(m.from(), currentStep);
                    return reactiveWeight > 0.0
                            ? reactiveWeight * factor
                            : factor;  // baza historyczna gdy brak pojazdów
                })
                .sum();
    }

    private double getFactor(Direction direction, int currentStep) {
        return historicalData
                .getOrDefault(direction, List.of())
                .stream()
                .filter(slot -> currentStep >= slot.fromStep() && currentStep <= slot.toStep())
                .mapToDouble(TimeSlot::factor)
                .findFirst()
                .orElse(1.0);  // domyślnie neutralny
    }

    @Override
    public boolean hasStarvingVehicle(Intersection intersection, int currentStep) {
        return reactive.hasStarvingVehicle(intersection, currentStep);
    }

    @Override
    public Set<Movement> getStarvingMovements(Intersection intersection, int currentStep) {
        return reactive.getStarvingMovements(intersection, currentStep);
    }
}
