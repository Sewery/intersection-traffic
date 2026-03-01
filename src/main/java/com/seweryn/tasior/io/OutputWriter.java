package com.seweryn.tasior.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.seweryn.tasior.statistics.Statistics;

import java.io.File;

public class OutputWriter {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void write(SimulationResult result, String outputFilePath) {
        try {
            ObjectNode root = mapper.createObjectNode();

            // stepStatuses
            ArrayNode stepStatuses = root.putArray("stepStatuses");
            for (StepStatus step : result.getStepStatuses()) {
                ObjectNode stepNode = stepStatuses.addObject();
                ArrayNode leftVehicles = stepNode.putArray("leftVehicles");
                step.leftVehicles().forEach(leftVehicles::add);
            }

            // statistics – tylko jeśli getStatistics było wywołane
            if (result.getStatistics() != null) {
                root.set("statistics", buildStatisticsNode(result.getStatistics()));
            }

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(outputFilePath), root);

        } catch (Exception e) {
            throw new RuntimeException("Failed to write output: " + outputFilePath, e);
        }
    }

    private static ObjectNode buildStatisticsNode(Statistics stats) {
        ObjectNode node = mapper.createObjectNode();
        node.put("totalVehiclesProcessed",stats.totalVehiclesProcessed());
        node.put("totalVehiclesStuck", stats.totalVehiclesStuck());
        node.put("averageWaitTime", stats.averageWaitTime());

        // percentiles
        ObjectNode percentiles = node.putObject("percentiles");
        percentiles.put("p50", stats.percentiles().p50());
        percentiles.put("p90", stats.percentiles().p90());
        percentiles.put("p95", stats.percentiles().p95());

        // blockedLanes
        ArrayNode blockedLanes = node.putArray("blockedLanes");
        stats.blockedLanes().forEach(bl -> {
            ObjectNode blNode = blockedLanes.addObject();
            blNode.put("road", bl.road().name());
            blNode.put("turn", bl.turn());
            blNode.put("vehiclesAffected", bl.vehiclesAffected());
            blNode.put("blockedFromStep", bl.blockedFromStep());
            blNode.put("blockedToStep", bl.blockedToStep() == -1
                    ? "still blocked"
                    : String.valueOf(bl.blockedToStep()));
        });

        // perRoadStats
        ObjectNode perRoad = node.putObject("perRoadStats");
        stats.perRoadStats().forEach((direction, roadStat) -> {
            ObjectNode roadNode = perRoad.putObject(direction.name());
            roadNode.put("avgWaitTime",        roadStat.avgWaitTime());
            roadNode.put("vehiclesProcessed",  roadStat.vehiclesProcessed());
            roadNode.put("vehiclesStuck",      roadStat.vehiclesStuck());
        });

        return node;
    }
}
