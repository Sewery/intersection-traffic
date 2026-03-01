package com.seweryn.tasior.controller;

import com.seweryn.tasior.model.Intersection;
import com.seweryn.tasior.model.Movement;

import java.util.Set;

public interface WeightCalculator {
    int MAX_WAIT_TIME = 10;

    double calculatePhaseWeight(Set<Movement> phase, Intersection intersection, int currentStep);
    boolean hasStarvingVehicle(Intersection intersection, int currentStep);
    Set<Movement> getStarvingMovements(Intersection intersection, int currentStep);
    void configure(double carPriority, double busPriority);
}