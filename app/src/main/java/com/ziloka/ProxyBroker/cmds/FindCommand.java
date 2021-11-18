package com.ziloka.ProxyBroker.cmds;

import com.google.gson.JsonParser;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.ziloka.ProxyBroker.services.ProxyService;
import com.ziloka.ProxyBroker.services.models.LookupResult;
import com.ziloka.ProxyBroker.services.models.ProxyType;
import com.ziloka.ProxyBroker.cmds.converters.IProxyTypeConverter;

import com.maxmind.geoip2.DatabaseReader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Command under proxybroker command
 */
// https://picocli.info/#_executing_subcommands
@SuppressWarnings("ALL")
@Command(name = "find")
public class FindCommand implements Callable<Integer> {

    private final Logger LOG = LogManager.getLogger(FindCommand.class);

    // https://picocli.info/apidocs/picocli/CommandLine.Option.html
    @Option(names = "--types", defaultValue = "http", type = IProxyTypeConverter.class, converter = IProxyTypeConverter.class)
    private List<ProxyType> types;

    @Option(names = "--countries", defaultValue = "")
    private String countries;

    @Option(names = "--lvl", defaultValue = "High")
    private String lvl;

    // https://picocli.info/#_handling_invalid_input
    @Option(names = {"--limit", "-l"}, defaultValue = "10", type = Integer.class)
    private int limit;

    @Option(names = {"--timeout", "-t"}, defaultValue = "1", type = Integer.class)
    private int timeout;

    @Option(names = {"--outfile", "-o"}, defaultValue = "")
    private String OutFile;

    @Option(names = {"--verbose", "-v"}, defaultValue = "false", type = Boolean.class)
    private boolean isVerbose;

    /**
     * Executes when user runs "proxybroker find"
     */
    @Override
    public Integer call() {

        try {

            if(isVerbose) Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
            else Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.OFF);

            ConcurrentHashMap<String, LookupResult> onlineProxies = new ConcurrentHashMap<>();

            LOG.debug("Collecting proxies");

            // Producer consumer pattern
            // https://dzone.com/articles/producer-consumer-pattern

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(
                    URI.create("http://httpbin.org/ip?json")
            ).build();
            HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());
            String externalIpAddr = JsonParser.parseString(res.body()).getAsJsonObject().get("origin").getAsString();

            InputStream database = getClass().getClassLoader().getResourceAsStream("GeoLite2-Country.mmdb");
            DatabaseReader dbReader = new DatabaseReader.Builder(database)
                    .build();
            final ProxyService ps = new ProxyService(dbReader, onlineProxies, externalIpAddr, types, countries, lvl, limit);

            Thread t1 = new Thread(new Runnable(){
                @Override
                public void run(){
                    try {
                        ps.produce();
                    } catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            });

            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ps.consume();
                    } catch (InterruptedException | IOException | GeoIp2Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            // Start both threads
            t1.start();
            t2.start();

            // t1 finishes before t2
            t1.join();
            t2.join();

            while (onlineProxies.size() <= limit){

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

            // ProxyBroker --outfile=proxies.txt
            if(OutFile.length() != 0){
                BufferedWriter writer = new BufferedWriter(new FileWriter(OutFile));
                writer.write(String.join("\n", onlineProxies.values().stream().map((i) -> i.proxyHost + ":" + i.proxyPort).collect(Collectors.joining())));
                writer.close();
                System.out.printf("Wrote %s checked proxies to %s\n", onlineProxies.size(), OutFile);
            } else {
              System.out.printf("Proxies length: %d\n", onlineProxies.size());
                onlineProxies.keySet().stream().limit(limit).forEach((entry) -> {
                    LookupResult value = onlineProxies.get(entry);
                    System.out.printf("<Proxy %s %s>\n", value.getCountryName(), entry);
                });
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return 0;

    }

}