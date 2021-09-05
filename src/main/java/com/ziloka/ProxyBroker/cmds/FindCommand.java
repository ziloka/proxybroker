
package com.ziloka.ProxyBroker.cmds;

import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import com.ziloka.ProxyBroker.services.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

// https://picocli.info/#_executing_subcommands
@SuppressWarnings("ALL")
@Command(name = "find")
public class FindCommand implements Runnable {

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

    public static void main(String[] args) {
        CommandLine cli = new CommandLine(new FindCommand());
        cli.setOptionsCaseInsensitive(true);
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }

    public void run() {

        try {
            Logger logger = LogManager.getLogger(FindCommand.class);
            HashMap<String, LookupResult> onlineProxies = new HashMap<>();

            logger.debug("Collecting proxies");

            ProxyCollector proxyProvider = new ProxyCollector(types, countries, lvl);
            proxyProvider.setSource();
            ArrayList<String> proxies = proxyProvider.getProxies(ProxyType.valueOf(types));

            // String#format
            // https://www.javatpoint.com/java-string-format
            logger.debug(String.format("There are %d unchecked proxies", proxies.size()));

            // Simple iteration on average takes more than 30+ minutes to check 200 proxies
            // On average takes ~20 seconds to check 200 proxies
            ExecutorService executorService = Executors.newCachedThreadPool();
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;

            File database = new File(ClassLoader.getSystemResource("GeoLite2-City.mmdb").toURI());
            DatabaseReader dbReader = new DatabaseReader.Builder(database)
                    .fileMode(Reader.FileMode.MEMORY_MAPPED)
                    .build();
            for (String proxy : proxies) {
                try {
                    ProxyThread proxyThread = new ProxyThread(dbReader, onlineProxies, proxy, types);
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
                    System.out.println(String.format("Country: %s, proxy: %s", value.countryName, entry));
                });
            }

        } catch (URISyntaxException | IOException e){
            e.printStackTrace();
        }

    }

}