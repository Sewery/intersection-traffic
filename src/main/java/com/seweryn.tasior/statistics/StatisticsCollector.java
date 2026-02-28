package com.seweryn.tasior.statistics;

import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.Turn;

import java.util.*;

public class StatisticsCollector {

    private final Map<String, Integer> arrivalSteps = new HashMap<>();
    private final Map<String, Integer> exitSteps = new HashMap<>();
    private final Map<String, Direction> vehicleDirections = new HashMap<>();
    private final Set<String> stuckVehicles = new HashSet<>();
    private final List<Statistics.BlockedLaneStat> blockedLanes = new ArrayList<>();

    private record BlockedLaneEntry(
            Direction road,
            Turn turn,
            int vehiclesAffected,
            int fromStep
    ) {}

    private final List<BlockedLaneEntry> blockedLaneEntries = new ArrayList<>();
    private final Map<String, Integer> unblockedSteps = new HashMap<>();

    public void onEvent(SimulationEvent event) {
        switch (event) {
            case SimulationEvent.VehicleArrived e -> {
                arrivalSteps.put(e.vehicleId(), e.step());
                vehicleDirections.put(e.vehicleId(), e.direction());
            }
            case SimulationEvent.VehicleExited e -> exitSteps.put(e.vehicleId(), e.step());
            case SimulationEvent.VehicleStuck e -> stuckVehicles.add(e.vehicleId());
            case SimulationEvent.LaneBlocked e -> blockedLaneEntries.add(
                    new BlockedLaneEntry(e.road(), e.turn(), e.vehiclesAffected(), e.step()));
            case SimulationEvent.LaneUnblocked e -> unblockedSteps.put(
                    key(e.road(), e.turn()), e.step());
        }
    }

    private String key(Direction road, Turn turn) {
        return road.name() + "_" + turn.name();
    }

    private List<Statistics.BlockedLaneStat> computeBlockedLanes() {
        return blockedLaneEntries.stream()
                .map(e -> new Statistics.BlockedLaneStat(
                        e.road(),
                        e.turn().name(),
                        e.vehiclesAffected(),
                        e.fromStep(),
                        unblockedSteps.getOrDefault(key(e.road(), e.turn()), -1)
                ))
                .toList();
    }

    public Statistics compute() {
        List<Integer> waitTimes = exitSteps.entrySet().stream()
                .map(e -> e.getValue() - arrivalSteps.get(e.getKey()))
                .sorted()
                .toList();

        return new Statistics(
                exitSteps.size(),
                stuckVehicles.size(),
                waitTimes.stream().mapToInt(i -> i).average().orElse(0.0),
                computePercentiles(waitTimes),
                computeBlockedLanes(),   // ← zamiast blockedLanes
                computePerRoadStats()
        );
    }

    private Statistics.Percentiles computePercentiles(List<Integer> sorted) {
        if (sorted.isEmpty()) return new Statistics.Percentiles(0, 0, 0);
        return new Statistics.Percentiles(
                percentile(sorted, 50),
                percentile(sorted, 90),
                percentile(sorted, 95)
        );
    }

    private double percentile(List<Integer> sorted, int p) {
        int index = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, index));
    }

    private Map<Direction, Statistics.RoadStat> computePerRoadStats() {
        Map<Direction, List<String>> byDirection = new HashMap<>();
        arrivalSteps.keySet().forEach(id ->
                byDirection.computeIfAbsent(vehicleDirections.get(id), k -> new ArrayList<>()).add(id));

        Map<Direction, Statistics.RoadStat> result = new EnumMap<>(Direction.class);
        byDirection.forEach((dir, vehicles) -> {
            List<Integer> waits = vehicles.stream()
                    .filter(exitSteps::containsKey)
                    .map(id -> exitSteps.get(id) - arrivalSteps.get(id))
                    .toList();

            result.put(dir, new Statistics.RoadStat(
                    waits.stream().mapToInt(i -> i).average().orElse(0.0),
                    waits.size(),
                    (int) vehicles.stream().filter(stuckVehicles::contains).count()
            ));
        });
        return result;
    }
}