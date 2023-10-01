package com.cpunisher;

import com.cpunisher.cli.CommandHydrate;
import com.cpunisher.cli.CommandTodolify;
import com.cpunisher.cli.SellotapeCLI;
import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        System.exit(new CommandLine(new SellotapeCLI()).execute(args));
    }
}