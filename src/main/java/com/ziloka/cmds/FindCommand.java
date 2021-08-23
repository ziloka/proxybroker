package com.ziloka.cmds;

import com.ziloka.services.ProxyCheckerService;
import com.ziloka.services.ProxyCheckerTask;
import com.ziloka.services.ProxyCollectorService;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

// https://picocli.info/#_executing_subcommands
@Command(name = "find")
public class FindCommand implements Callable<Integer> {

    // https://picocli.info/apidocs/picocli/CommandLine.Option.html
    @Option(names = "--types")
    private String types;

    @Option(names = "--countries")
    private String countries;

    @Option(names = "--lvl")
    private String lvl;

    @Option(names = {"--limit", "-l"})
    private int limit;

    @Option(names = {"--outfile", "-o"})
    private String OutFile;

    public static void main(String[] args) {
        CommandLine cli = new CommandLine(new FindCommand());
        cli.setOptionsCaseInsensitive(true);
        System.out.println("HEllo");
        int exitCode = cli.execute(args);

        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        // Business logic

        ProxyCollectorService proxyProvider = new ProxyCollectorService(types, countries, lvl, limit);
        proxyProvider.setSources();
        ArrayList<String> proxies = proxyProvider.getProxies();
        // Main Worker Thread
//        ProxyCheckerService proxyCheckerService = new ProxyCheckerService();
//        proxyCheckerService.check(proxies.get(0));

        // Worker Threads
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        for (String proxy : proxies) {
            ProxyCheckerTask proxyCheckerTask = new ProxyCheckerTask(proxy);
            executor.execute(proxyCheckerTask);
        }

        return 0;
    }

}
