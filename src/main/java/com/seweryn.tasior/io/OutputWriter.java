package com.seweryn.tasior.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;

public class OutputWriter {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void write(SimulationResult result, String outputFilePath) {
        try {
            ObjectNode root = mapper.createObjectNode();
            ArrayNode stepStatuses = root.putArray("stepStatuses");

            for (StepStatus step : result.getStepStatuses()) {
                ObjectNode stepNode = stepStatuses.addObject();
                ArrayNode leftVehicles = stepNode.putArray("leftVehicles");
                step.leftVehicles().forEach(leftVehicles::add);
            }

            mapper.writerWithDefaultPrettyPrinter()
                  .writeValue(new File(outputFilePath), root);

        } catch (Exception e) {
            throw new RuntimeException("Failed to write output: " + outputFilePath, e);
        }
    }
}
