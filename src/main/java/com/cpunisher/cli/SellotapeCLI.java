package com.cpunisher.cli;

import com.cpunisher.dep.DepGraph;
import picocli.CommandLine;

@CommandLine.Command(name = "java-sellotape", subcommands = {
        CommandTodolify.class,
        CommandHydrate.class,
        CommandDepMetric.class,
})
public class SellotapeCLI implements Runnable {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;


    @Override
    public void run() {
        spec.commandLine().usage(System.err);
    }
}
