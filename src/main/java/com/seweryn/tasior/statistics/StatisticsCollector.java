package com.seweryn.tasior.statistics;

import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.Turn;

import java.util.*;

public class StatisticsCollector {

    private final Map<String, Integer> arrivalSteps = new HashMap<>();
    private final Map<String, Integer> exitSteps = new HashMap<>();
    private final Map<String, Direction> vehicleDirections = new HashMap<>();
    private final Set<String> stuckVehicles = new HashSet<>();

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
            case SimulationEvent.VehicleArrived(String vehicleId, Direction direction, int step) -> {
                arrivalSteps.put(vehicleId, step);
                vehicleDirections.put(vehicleId, direction);
            }
            case SimulationEvent.VehicleExited(String vehicleId, int step) ->
                    exitSteps.put(vehicleId, step);
            case SimulationEvent.VehicleStuck(String vehicleId, Direction _) ->
                    stuckVehicles.add(vehicleId);
            case SimulationEvent.LaneBlocked(Direction road, Turn turn, int vehiclesAffected, int step) ->
                    blockedLaneEntries.add(new BlockedLaneEntry(road, turn, vehiclesAffected, step));
            case SimulationEvent.LaneUnblocked(Direction road, Turn turn, int step) ->
                    unblockedSteps.put(key(road, turn), step);
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
                computeBlockedLanes(),
                computePerRoadStats()
        );
    }

    private Statistics.Percentiles computePercentiles(List<Integer> sorted) {
        if (sorted.isEmpty()) return new Statistics.Percentiles(0, 0, 0);
        return new Statistics.Percentiles(
                percentile(sorted, 50),
                percentile(sorted, 75),
                percentile(sorted, 90)
        );
    }

    private double percentile(List<Integer> sorted, int p) {
        int index = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, index));
    }

    private Map<Direction, Statistics.RoadStat> computePerRoadStats() {
        Map<Direction, List<String>> byDirection = new HashMap<>();
        arrivalSteps.keySet().forEach(id ->
                byDirection.computeIfAbsent(vehicleDirections.get(id), _ -> new ArrayList<>()).add(id));

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