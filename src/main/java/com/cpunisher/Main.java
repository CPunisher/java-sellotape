package com.cpunisher;

import com.cpunisher.cli.CommandTodolify;
import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        System.exit(new CommandLine(new CommandTodolify()).execute(args));
    }
}