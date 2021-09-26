package com.ziloka.ProxyBroker;

import com.ziloka.ProxyBroker.cmds.FindCommand;
import com.ziloka.ProxyBroker.cmds.ServeCommand;
import picocli.CommandLine;
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
        CommandLine cli = new CommandLine(new ProxyBroker());
        cli.setOptionsCaseInsensitive(false);
        if(args.length == 0) System.out.println(cli.getUsageMessage());
        int exitCode = cli.execute(args);
//        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        return 0;
    }

}