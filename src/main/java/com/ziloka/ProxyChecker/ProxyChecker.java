
package com.ziloka.ProxyChecker;

import com.ziloka.ProxyChecker.cmds.FindCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

// https://picocli.info/apidocs/picocli/CommandLine.Parameters.html
// https://github.com/remkop/picocli/blob/master/picocli-spring-boot-starter/README.md
// https://picocli.info/#_registering_subcommands_declaratively
@Command(name = "proxychecker", description = "Find or serve proxy server", subcommands = {
        FindCommand.class
})
public class ProxyChecker implements Callable<Integer> {

    // https://picocli.info/apidocs/picocli/CommandLine.Option.html
    @Option(names = {"--help", "-help"}, usageHelp = true, description = "An open source tool to find public proxies or serve a local proxy server that distributes requests to a pool of found HTTP proxies")
    private boolean help;

    public static void main(String[] args) {
        CommandLine cli = new CommandLine(new ProxyChecker());
        cli.setOptionsCaseInsensitive(false);
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        return 0;
    }

}