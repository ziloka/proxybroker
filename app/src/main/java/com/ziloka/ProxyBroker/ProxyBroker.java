package com.ziloka.ProxyBroker;

import com.ziloka.ProxyBroker.subcmds.FindCommand;
import com.ziloka.ProxyBroker.subcmds.ServeCommand;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

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
public class ProxyBroker implements Runnable {

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
        ParseResult parseResult = cli.parseArgs(args);
        int exitCode = cli.execute(args);
        if(parseResult.subcommand() != null){
            // Don't exit cli if we are hosting web server
            if(!parseResult.subcommand().commandSpec().name().equals("serve")) System.exit(exitCode);
        }
    }

    @Override
    public void run() {

    }
}