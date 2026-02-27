package com.seweryn.tasior;

import com.seweryn.tasior.commands.AddVehicleCommand;
import com.seweryn.tasior.commands.Command;
import com.seweryn.tasior.commands.ConfigureAlgorithmCommand;
import com.seweryn.tasior.commands.UpdateLaneStatusCommand;

import com.seweryn.tasior.controller.TrafficController;
import com.seweryn.tasior.controller.WeightCalculator;

import com.seweryn.tasior.io.SimulationResult;
import com.seweryn.tasior.io.StepStatus;
import com.seweryn.tasior.io.InputParser;

import com.seweryn.tasior.model.Intersection;
import com.seweryn.tasior.model.Lane;
import com.seweryn.tasior.model.Road;
import com.seweryn.tasior.model.Vehicle;

import java.util.List;
import java.util.ArrayList;

public class SimulationEngine {
    private final List<Command> commands;
    private final Intersection intersection;
    private final TrafficController controller;
    private final SimulationResult result;
    private int stepCounter;

    public SimulationEngine(String inputFile) {
        this.commands = InputParser.parse(inputFile);
        this.intersection = new Intersection();
        this.controller = new TrafficController(intersection, new WeightCalculator());
        this.result = new SimulationResult();
        this.stepCounter = 0;
    }

    public SimulationResult runSimulation() {
        for (Command command : commands) {
            switch (command.getType()) {
                case ADD_VEHICLE        -> handleAddVehicle((AddVehicleCommand) command);
                case STEP               -> handleStep();
                case CONFIGURE_ALGORITHM -> handleConfigure((ConfigureAlgorithmCommand) command);
                case UPDATE_LANE_STATUS  -> handleUpdateLane((UpdateLaneStatusCommand) command);
            }
        }
        return result;
    }

    private void handleUpdateLane(UpdateLaneStatusCommand command) {
    }

    private void handleConfigure(ConfigureAlgorithmCommand command) {
    }

    private void handleStep() {
        List<String> leftThisStep = new ArrayList<>();

        controller.executeStep(stepCounter);

        // najpierw zbierz pojazdy, które mogą wyjechać (światło GREEN)
        for (Road road : intersection.getRoads()) {
            for (Lane lane : road.getLanes()) {
                if (lane.getTrafficLight().isGreen() && !lane.isEmpty()) {
                    leftThisStep.add(lane.pollVehicle().vehicleId());
                }
            }
        }

        result.addStep(new StepStatus(leftThisStep));
        stepCounter++;
    }

    private void handleAddVehicle(AddVehicleCommand command){
        Road road = intersection.getRoad(command.startRoad());
        road.addVehicleToLane(
                command.endRoad(),
                new Vehicle(stepCounter,command.vehicleId())
        );
    }

}