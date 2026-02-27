package com.seweryn.tasior.commands;

public record ConfigureAlgorithmCommand(double carPriority, double busPriority) implements Command {
    @Override
    public CommandType getType() {
        return CommandType.CONFIGURE_ALGORITHM;
    }
}
