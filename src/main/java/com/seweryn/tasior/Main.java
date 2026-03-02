package com.seweryn.tasior;

import com.seweryn.tasior.io.OutputWriter;
import com.seweryn.tasior.io.SimulationIOException;
import com.seweryn.tasior.io.SimulationResult;

public class Main {
    static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Main <inputFile> <outputFile>");
            System.exit(1);
        }

        try {
            SimulationEngine engine = new SimulationEngine(args[0]);
            SimulationResult result = engine.runSimulation();
            OutputWriter.write(result, args[1]);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input: " + e.getMessage());
            System.exit(1);
        } catch (SimulationIOException e) {
            System.err.println("IO error: " + e.getMessage());
            System.exit(1);
        }
    }
}
