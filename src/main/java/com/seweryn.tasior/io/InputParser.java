package com.seweryn.tasior.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seweryn.tasior.commands.Command;
import com.seweryn.tasior.commands.ConfigureAlgorithmCommand;
import com.seweryn.tasior.commands.UpdateLaneStatusCommand;
import com.seweryn.tasior.commands.AddVehicleCommand;
import com.seweryn.tasior.model.Direction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
                    case "step"               -> Command.STEP;
                    case "configureAlgorithm" -> new ConfigureAlgorithmCommand(
                        node.get("carPriority").asDouble(1.0),
                        node.get("busPriority").asDouble(5.0)
                    );
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
}
