
package com.ziloka.ProxyChecker.cmds;

import com.ziloka.ProxyChecker.services.ProxyCheckerTask;
import com.ziloka.ProxyChecker.services.ProxyCollectorService;
import com.ziloka.ProxyChecker.services.ProxyType;
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
    @Option(names = "--types", defaultValue = "http")
    private String types;

    @Option(names = "--countries", defaultValue = "")
    private String countries;

    @Option(names = "--lvl", defaultValue = "High")
    private String lvl;

    @Option(names = {"--limit", "-l"}, defaultValue = "10", type = Integer.class)
    private int limit;

    @Option(names = {"--outfile", "-o"}, defaultValue = "")
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
        proxyProvider.setSource();
        ArrayList<String> proxies = proxyProvider.getProxies(ProxyType.valueOf(types));

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

        onlineProxies.entrySet().forEach((entry) -> {
//           System.out.println((entry));
        });

        System.out.println("\nFinished all threads");

        return 0;
    }

}