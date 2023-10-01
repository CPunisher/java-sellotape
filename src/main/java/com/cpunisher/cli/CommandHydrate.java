package com.cpunisher.cli;

import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;

@CommandLine.Command(name = "hydrate")
public class CommandHydrate implements Runnable {

    @CommandLine.Parameters(paramLabel = "PROJECT")
    File project;

    @CommandLine.Option(names = { "-o", "--output" })
    Path output;

    @Override
    public void run() {
    }
}
