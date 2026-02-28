package com.seweryn.tasior;

import com.seweryn.tasior.commands.AddVehicleCommand;
import com.seweryn.tasior.commands.Command;
import com.seweryn.tasior.commands.ConfigureAlgorithmCommand;
import com.seweryn.tasior.commands.UpdateLaneStatusCommand;

import com.seweryn.tasior.controller.HistoricalWeightCalculator;
import com.seweryn.tasior.controller.TrafficController;
import com.seweryn.tasior.controller.ReactiveWeightCalculator;

import com.seweryn.tasior.controller.WeightCalculator;
import com.seweryn.tasior.io.SimulationResult;
import com.seweryn.tasior.io.StepStatus;
import com.seweryn.tasior.io.InputParser;

import com.seweryn.tasior.model.Intersection;
import com.seweryn.tasior.model.Lane;
import com.seweryn.tasior.model.Road;
import com.seweryn.tasior.model.Vehicle;
import com.seweryn.tasior.statistics.SimulationEvent;
import com.seweryn.tasior.statistics.StatisticsCollector;

import java.util.List;
import java.util.ArrayList;

public class SimulationEngine {
    private final List<Command> commands;
    private final Intersection intersection;
    private TrafficController controller;
    private final SimulationResult result;
    private final StatisticsCollector statisticsCollector = new StatisticsCollector();
    private int stepCounter;

    public SimulationEngine(String inputFile) {
        this.commands = InputParser.parse(inputFile);
        this.intersection = new Intersection();
        this.controller = new TrafficController(intersection, new ReactiveWeightCalculator());
        this.result = new SimulationResult();
        this.stepCounter = 0;
    }


    public SimulationResult runSimulation() {
        for (Command command : commands) {
            switch (command.getType()) {
                case ADD_VEHICLE -> handleAddVehicle((AddVehicleCommand) command);
                case STEP -> handleStep();
                case CONFIGURE_ALGORITHM -> handleConfigure((ConfigureAlgorithmCommand) command);
                case UPDATE_LANE_STATUS -> handleUpdateLane((UpdateLaneStatusCommand) command);
                case GET_STATISTICS -> handleGetStatistics();
            }
        }
        return result;
    }

    private void handleGetStatistics() {
        result.setStatistics(statisticsCollector.compute());
    }

    private void handleUpdateLane(UpdateLaneStatusCommand command) {
        Road road = intersection.getRoad(command.road());
        road.getLaneByTurn(command.turn()).ifPresent(lane -> {
            if (command.blocked()) {
                lane.getWaitingVehicles().forEach(v ->
                        statisticsCollector.onEvent(
                                new SimulationEvent.VehicleStuck(v.vehicleId(), command.road())));
                if (!lane.getWaitingVehicles().isEmpty()) {
                    statisticsCollector.onEvent(new SimulationEvent.LaneBlocked(
                            command.road(), command.turn(),
                            lane.getWaitingVehicles().size(), stepCounter));  // ← step
                }
            } else {
                statisticsCollector.onEvent(new SimulationEvent.LaneUnblocked(
                        command.road(), command.turn(), stepCounter));  // ← odblokowanie
            }
            lane.setBlocked(command.blocked());
        });
    }

    private void handleConfigure(ConfigureAlgorithmCommand command) {
        WeightCalculator calculator = switch (command.mode()) {
            case HISTORICAL -> new HistoricalWeightCalculator(command.historicalData());
            case REACTIVE   -> new ReactiveWeightCalculator();
        };
        calculator.configure(command.carPriority(), command.busPriority());
        controller = new TrafficController(intersection, calculator);
    }

    private void handleStep() {
        List<String> leftThisStep = new ArrayList<>();

        // faza 1 – pojazdy które były na skrzyżowaniu (CROSSING) opuszczają je
        for (Road road : intersection.getRoads()) {
            for (Lane lane : road.getLanes()) {
                lane.finishCrossing()
                        .map(Vehicle::vehicleId)
                        .ifPresent(id -> {
                            leftThisStep.add(id);
                            statisticsCollector.onEvent(
                                    new SimulationEvent.VehicleExited(id, stepCounter));
                        });
            }
        }

        // zmień fazę świateł
        controller.executeStep(stepCounter);

        // faza 2 – pojazdy z zielonych pasów wjeżdżają na skrzyżowanie
        for (Road road : intersection.getRoads()) {
            for (Lane lane : road.getLanes()) {
                if (lane.isPassable() && !lane.isEmpty() && !lane.isCrossing()) {
                    lane.startCrossing();
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
                new Vehicle(command.vehicleId(),stepCounter)
        );
        statisticsCollector.onEvent(new SimulationEvent.VehicleArrived(
                command.vehicleId(), command.startRoad(), stepCounter));
    }

}