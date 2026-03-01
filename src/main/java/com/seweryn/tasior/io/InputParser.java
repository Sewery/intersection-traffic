package com.seweryn.tasior.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seweryn.tasior.commands.*;
import com.seweryn.tasior.controller.TimeSlot;
import com.seweryn.tasior.controller.TrafficDefaults;
import com.seweryn.tasior.model.Direction;
import com.seweryn.tasior.model.Turn;
import com.seweryn.tasior.model.VehicleType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Command> parse(String inputFilePath) {
        try {
            JsonNode root = mapper.readTree(new File(inputFilePath));
            JsonNode commands = root.get("commands");
            List<Command> result = new ArrayList<>();

            for (JsonNode node : commands) {
                String type = node.get("type").asText();
                Command command = switch (type) {
                    case "addVehicle" -> new AddVehicleCommand(
                            Direction.fromString(node.get("startRoad").asText()),
                            Direction.fromString(node.get("endRoad").asText()),
                            node.get("vehicleId").asText(),
                            node.has("vehicleType")
                                    ? VehicleType.valueOf(node.get("vehicleType").asText().toUpperCase())
                                    : VehicleType.CAR
                    );
                    case "step"-> Command.STEP;
                    case "configureAlgorithm" -> parseConfigureAlgorithm(node);
                    case "updateLaneStatus"   -> new UpdateLaneStatusCommand(
                            Direction.fromString(node.get("road").asText()),
                            Turn.valueOf(node.get("turn").asText().toUpperCase()),
                            node.get("blocked").asBoolean()
                    );
                    case "getStatistics"    -> Command.GET_STATISTICS;
                    default -> throw new IllegalArgumentException("Unknown command: " + type);
                };
                result.add(command);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse input: " + inputFilePath, e);
        }
    }

    private static ConfigureAlgorithmCommand parseConfigureAlgorithm(JsonNode node) {
        return new ConfigureAlgorithmCommand(
                getDouble(node, "carPriority"),
                getDouble(node, "busPriority"),
                node.has("mode")
                        ? AlgorithmMode.valueOf(node.get("mode").asText().toUpperCase())
                        : null,
                node.has("historicalData") ? parseHistoricalData(node.get("historicalData")) : null,
                getInt(node, "maxWaitTime"),
                getInt(node, "yellowTime")
        );
    }

    private static Double getDouble(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asDouble() : null;
    }

    private static Integer getInt(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asInt() : null;
    }
    private static Map<Direction, List<TimeSlot>> parseHistoricalData(JsonNode historicalNode) {
        Map<Direction, List<TimeSlot>> historicalData = new HashMap<>();

        historicalNode.fields().forEachRemaining(entry -> {
            Direction direction = Direction.fromString(entry.getKey());
            List<TimeSlot> slots = new ArrayList<>();

            for (JsonNode slotNode : entry.getValue()) {
                slots.add(new TimeSlot(
                        slotNode.get("fromStep").asInt(),
                        slotNode.get("toStep").asInt(),
                        slotNode.get("factor").asDouble()
                ));
            }

            historicalData.put(direction, slots);
        });

        return historicalData;
    }
}
