
package com.ziloka.ProxyChecker.cmds;

import com.ziloka.ProxyChecker.services.ProxyCheckerService;
import com.ziloka.ProxyChecker.services.ProxyCheckerTask;
import com.ziloka.ProxyChecker.services.ProxyCollectorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
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
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {

        Logger logger = LogManager.getLogger(FindCommand.class);
        HashMap<String, Boolean> onlineProxies = new HashMap<String, Boolean>();

        logger.debug("Collecting proxies");

        ProxyCollectorService proxyProvider = new ProxyCollectorService(types, countries, lvl);
        proxyProvider.setSources();
        ArrayList<String> proxies = proxyProvider.getProxies(types);
        // String#format
        // https://www.javatpoint.com/java-string-format
        logger.debug(String.format("There are %d unchecked proxies", proxies.size()));

        // Simple iteration on average takes more than 30+ minutes to check 200 proxies
        // On average takes ~20 seconds to check 200 proxies
        ExecutorService executorService = Executors.newCachedThreadPool();
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;
        for (String proxy : proxies) {
            try {
                ProxyCheckerTask proxyCheckerTask = new ProxyCheckerTask(proxy, types, onlineProxies);
                executorService.submit(proxyCheckerTask);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        logger.debug(String.format("Multithreading ProxyCheckTask.class using %d threads", threadPoolExecutor.getActiveCount()));

        executorService.shutdown();
        // Wait for all threads states to be terminated
        while (!executorService.isTerminated()){

        }
        logger.debug(String.format("There are %d online proxies", onlineProxies.size()));

        System.out.println("\nFinished all threads");

        return 0;
    }

}