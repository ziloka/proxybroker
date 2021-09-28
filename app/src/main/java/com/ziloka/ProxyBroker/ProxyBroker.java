package com.ziloka.ProxyBroker;

import com.ziloka.ProxyBroker.handlers.ShortErrorMessageHandler;
import com.ziloka.ProxyBroker.subcmds.FindCommand;
import com.ziloka.ProxyBroker.subcmds.ServeCommand;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * ProxyBroker class
 */
// https://picocli.info/apidocs/picocli/CommandLine.Parameters.html
// https://github.com/remkop/picocli/blob/master/picocli-spring-boot-starter/README.md
// https://picocli.info/#_registering_subcommands_declaratively
@Command(name = "proxychecker", description = "Find or serve proxy server", subcommands = {
        FindCommand.class,
        ServeCommand.class
})
public class ProxyBroker implements Callable<Integer> {

    // https://picocli.info/apidocs/picocli/CommandLine.Option.html
    @Option(names = {"--help", "-help"}, usageHelp = true, description = "An open source tool to find public proxies or serve a local proxy server that distributes requests to a pool of found HTTP proxies")
    private boolean help;

    /**
     * @param args System arguments
     */
    public static void main(String[] args) {
        CommandLine cli = new CommandLine(new ProxyBroker())
                .setParameterExceptionHandler(new ShortErrorMessageHandler());
        cli.setOptionsCaseInsensitive(false);
        if(args.length == 0) System.out.println(cli.getUsageMessage());
        ParseResult parseResult = cli.parseArgs(args);
        // https://picocli.info/#_handling_errors
        int exitCode = cli.execute(args);
        // implement Callable if command exits, implement Runnable if command does not exit
        if(parseResult.subcommand() != null){
            if(parseResult.subcommand().commandSpec().userObject() instanceof Callable){
                System.exit(exitCode);
            }
        }
    }

    @Override
    public Integer call() {
        return 0;
    }
}