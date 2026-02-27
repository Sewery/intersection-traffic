package com.seweryn.tasior;

import com.seweryn.tasior.io.OutputWriter;
import com.seweryn.tasior.io.SimulationResult;

public class Main {
    static void main(String[] args) {
        SimulationEngine engine = new SimulationEngine(args[0]);
        SimulationResult result = engine.runSimulation();
        OutputWriter.write(result, args[1]);
    }
}
