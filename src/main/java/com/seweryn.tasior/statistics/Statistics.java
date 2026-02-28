package com.seweryn.tasior.statistics;
import com.seweryn.tasior.model.Direction;

import java.util.List;
import java.util.Map;

public record Statistics(
        int totalVehiclesProcessed,
        int totalVehiclesStuck,
        double averageWaitTime,
        Percentiles percentiles,
        List<BlockedLaneStat> blockedLanes,
        Map<Direction, RoadStat> perRoadStats
) {
    public record Percentiles(double p50, double p90, double p95) {}
    public record BlockedLaneStat(Direction road, String turn, int vehiclesAffected, int blockedFromStep, int blockedToStep) {}
    public record RoadStat(double avgWaitTime, int vehiclesProcessed, int vehiclesStuck) {}
}
