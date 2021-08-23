package com.ziloka.cmds;

import com.ziloka.services.ProxyCheckerService;
import com.ziloka.services.ProxyCheckerTask;
import com.ziloka.services.ProxyCollectorService;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// https://picocli.info/#_executing_subcommands
@Command(name = "find")
public class FindCommand implements Callable<Integer> {

    // https://picocli.info/apidocs/picocli/CommandLine.Option.html
    @Option(names = "--types")
    private static String types;

    @Option(names = "--countries")
    private static String countries;

    @Option(names = "--lvl")
    private static String lvl;

    @Option(names = {"--limit", "-l"})
    private static int limit;

    @Option(names = {"--outfile", "-o"})
    private static String OutFile;

    static ArrayList<String> proxies;

    public static void main(String[] args) {
        CommandLine cli = new CommandLine(new FindCommand());
        cli.setOptionsCaseInsensitive(true);
        System.out.println("HEllo");

        int exitCode = cli.execute(args);

        setParams ();
        for (String proxy : proxies) {
            ProxyCheckerTask proxyCheckerTask = new ProxyCheckerTask(proxy);
            proxyCheckerTask.run();
        }
        System.exit(exitCode);
    }

    private static void setParams (){
        ProxyCollectorService proxyProvider = new ProxyCollectorService(types, countries, lvl, limit);
        proxyProvider.setSources();
        proxies = proxyProvider.getProxies();
    }

    @Override
    public Integer call() throws Exception {
        // Business logic


        try {
            ProxyCheckerService proxyCheckerService = new ProxyCheckerService();
            proxyCheckerService.check(proxy);
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Main Worker Thread
//        ProxyCheckerService proxyCheckerService = new ProxyCheckerService();
//        proxyCheckerService.check("222.74.202.242:8081");

        // https://stackoverflow.com/questions/12835077/java-multithread-multiple-requests-approach
        // Worker Threads
//        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
//        executor.setKeepAliveTime(60, TimeUnit.SECONDS);
//        for (String proxy : proxies) {
//            ProxyCheckerTask proxyCheckerTask = new ProxyCheckerTask(proxy);
//            executor.execute(proxyCheckerTask);
//        }

        return 0;
    }

}
