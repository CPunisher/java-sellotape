package com.cpunisher.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "java-sellotape", subcommands = {
        CommandTodolify.class,
        CommandHydrate.class,
})
public class SellotapeCLI implements Runnable {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.err);
    }
}
