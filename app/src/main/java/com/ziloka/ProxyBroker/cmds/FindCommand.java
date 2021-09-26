package com.ziloka.ProxyBroker.cmds;

import com.ziloka.ProxyBroker.services.ProxyCollector;
import com.ziloka.ProxyBroker.services.ProxyThread;
import com.ziloka.ProxyBroker.services.models.LookupResult;
import com.ziloka.ProxyBroker.services.models.ProxyType;

import com.maxmind.geoip2.DatabaseReader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
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

    private static final Logger LOG = LogManager.getLogger(FindCommand.class);

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
     * Executes when user runs "proxybroker find"
     */
    @Override
    public void run() {

        try {

            if(isVerbose) Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
            else Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.OFF);

            ConcurrentHashMap<String, LookupResult> onlineProxies = new ConcurrentHashMap<>();

            LOG.debug("Collecting proxies");

            ProxyCollector proxyProvider = new ProxyCollector(types, countries);
            ArrayList<String> proxies = proxyProvider.getProxies(ProxyType.valueOf(types));

            // String#format
            // https://www.javatpoint.com/java-string-format
            LOG.debug(String.format("There are %d unchecked proxies", proxies.size()));

            // Simple iteration on average takes more than 30+ minutes to check 200 proxies
            // On average takes ~20 seconds to check 200 proxies
            // https://www.baeldung.com/java-future#more-multithreading-with-thread-pools
            ExecutorService executorService = Executors.newCachedThreadPool();
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;

            InputStream database = getClass().getClassLoader().getResourceAsStream("GeoLite2-Country.mmdb");
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

            LOG.debug(String.format("Multithreading ProxyCheckTask.class using %d threads", threadPoolExecutor.getActiveCount()));

            executorService.shutdown();
            // Wait for all threads states to be terminated or until x amount of proxies are received
            while (!executorService.isTerminated() && !(onlineProxies.size() >= limit)){

            }

            // Filter good proxies & bad proxies
            // https://www.baeldung.com/java-concurrentmodificationexception
            // https://stackoverflow.com/a/4078601
            for(Iterator<String> iterator = onlineProxies.keySet().iterator(); iterator.hasNext();){
                String key = iterator.next();
                LookupResult value = onlineProxies.get(key);
                if(value.getCountryName().equals("China")){
                    onlineProxies.remove(key);
                }
            }

            LOG.debug(String.format("There are %d online proxies", onlineProxies.size()));

            onlineProxies.keySet().stream().limit(limit).forEach((entry) -> {
                LookupResult value = onlineProxies.get(entry);
                System.out.printf("<Proxy %s %s>\n", value.getCountryName(), entry);
            });

        } catch (Exception e){
            e.printStackTrace();
        }

    }

}