package com.ziloka.ProxyBroker.cmds;

import com.maxmind.geoip2.DatabaseReader;
import com.ziloka.ProxyBroker.services.*;
import com.ziloka.ProxyBroker.services.models.LookupResult;
import com.ziloka.ProxyBroker.services.models.ProxyType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Command under proxybroker command
 */
// https://picocli.info/#_executing_subcommands
@SuppressWarnings("ALL")
@Command(name = "find")
public class FindCommand implements Runnable {

    private static final Logger logger = LogManager.getLogger(FindCommand.class);

    // https://picocli.info/apidocs/picocli/CommandLine.Option.html
    @Option(names = "--types", defaultValue = "http")
    private String types;

    @Option(names = "--countries", defaultValue = "")
    private String countries;

    @Option(names = "--lvl", defaultValue = "High")
    private String lvl;

    // https://picocli.info/#_handling_invalid_input
    @Option(names = {"--limit", "-l"}, defaultValue = "10", type = Integer.class)
    private int limit;

    @Option(names = {"--timeout", "-t"}, defaultValue = "8", type = Integer.class)
    private int timeout;

    @Option(names = {"--outfile", "-o"}, defaultValue = "")
    private String OutFile;

    @Option(names = {"--verbose", "-v"}, defaultValue = "false", type = Boolean.class)
    private boolean isVerbose;

    /**
     * Set commandline options
     * @param args System arguments
     */
    public static void main(String[] args) {
        CommandLine cli = new CommandLine(new FindCommand());
        cli.setOptionsCaseInsensitive(true);
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }

    /**
     * Executes when user runs "proxybroker find"
     */
    @Override
    public void run() {

        try {

            Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.OFF);

//            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
//            Configuration config = ctx.getConfiguration();
//            LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
//
//            if(isVerbose) loggerConfig.setLevel(Level.DEBUG);
//            else loggerConfig.setLevel(Level.OFF);
//
//            ctx.updateLoggers();

            HashMap<String, LookupResult> onlineProxies = new HashMap<>();

            logger.debug("Collecting proxies");

            ProxyCollector proxyProvider = new ProxyCollector(types, countries);
            ArrayList<String> proxies = proxyProvider.getProxies(ProxyType.valueOf(types));

            // String#format
            // https://www.javatpoint.com/java-string-format
            logger.debug(String.format("There are %d unchecked proxies", proxies.size()));

            // Simple iteration on average takes more than 30+ minutes to check 200 proxies
            // On average takes ~20 seconds to check 200 proxies
            ExecutorService executorService = Executors.newCachedThreadPool();
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;

            InputStream database = getClass().getClassLoader().getSystemResourceAsStream("GeoLite2-Country.mmdb");
            DatabaseReader dbReader = new DatabaseReader.Builder(database)
                    .build();
            for (String proxy : proxies) {
                try {
                    ProxyThread proxyThread = new ProxyThread(dbReader, onlineProxies, proxy, types, lvl);
                    executorService.submit(proxyThread);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            logger.debug(String.format("Multithreading ProxyCheckTask.class using %d threads", threadPoolExecutor.getActiveCount()));

            executorService.shutdown();
            // Wait for all threads states to be terminated or until x amount of proxies are received
            while (!executorService.isTerminated() && !(onlineProxies.size() >= limit)){

            }
            logger.debug(String.format("There are %d online proxies", onlineProxies.size()));

            synchronized (onlineProxies){
                onlineProxies.keySet().stream().limit(limit).forEach((entry) -> {
                    LookupResult value = onlineProxies.get(entry);
                    System.out.printf("<Proxy %s %s>\n", value.getCountryName(), entry);
                });
            }

        } catch (IOException e){
            e.printStackTrace();
        }

    }

}