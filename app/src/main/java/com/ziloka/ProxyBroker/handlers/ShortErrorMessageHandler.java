package com.ziloka.ProxyBroker.handlers;

import picocli.CommandLine;

import java.io.PrintWriter;

public class ShortErrorMessageHandler implements CommandLine.IParameterExceptionHandler {

    public int handleParseException(CommandLine.ParameterException ex, String[] args) {
        System.out.println("gets here");
        CommandLine cmd = ex.getCommandLine();
        PrintWriter err = cmd.getErr();

        // if tracing at DEBUG level, show the location of the issue
        if ("DEBUG".equalsIgnoreCase(System.getProperty("picocli.trace"))) {
            err.println(cmd.getColorScheme().stackTraceText(ex));
        }

        err.println(cmd.getColorScheme().errorText(ex.getMessage())); // bold red
        CommandLine.UnmatchedArgumentException.printSuggestions(ex, err);
        err.print(cmd.getHelp().fullSynopsis());

        CommandLine.Model.CommandSpec spec = cmd.getCommandSpec();
        err.printf("Try '%s --help' for more information.%n", spec.qualifiedName());

        return cmd.getExitCodeExceptionMapper() != null
                ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                : spec.exitCodeOnInvalidInput();
    }

}
