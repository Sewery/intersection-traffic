package com.seweryn.tasior.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seweryn.tasior.commands.*;
import com.seweryn.tasior.controller.TimeSlot;
import com.seweryn.tasior.model.Direction;

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
                            node.get("vehicleId").asText()
                    );
                    case "step"-> Command.STEP;
                    case "configureAlgorithm" -> parseConfigureAlgorithm(node);
                    case "updateLaneStatus"   -> new UpdateLaneStatusCommand();
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
        double carPriority = node.get("carPriority").asDouble(1.0);
        double busPriority = node.get("busPriority").asDouble(5.0);

        AlgorithmMode mode = node.has("mode")
                ? AlgorithmMode.valueOf(node.get("mode").asText().toUpperCase())
                : AlgorithmMode.REACTIVE;

        Map<Direction, List<TimeSlot>> historicalData = null;
        if (mode == AlgorithmMode.HISTORICAL && node.has("historicalData")) {
            historicalData = parseHistoricalData(node.get("historicalData"));
        }

        return new ConfigureAlgorithmCommand(carPriority, busPriority, mode, historicalData);
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
