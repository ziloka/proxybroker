package com.ziloka.ProxyBroker.services.web;

import com.google.gson.Gson;
import com.maxmind.geoip2.DatabaseReader;
import com.ziloka.ProxyBroker.services.ProxyCollector;
import com.ziloka.ProxyBroker.services.ProxyThread;
import com.ziloka.ProxyBroker.services.models.LookupResult;

import com.ziloka.ProxyBroker.services.models.ProxyType;
import com.ziloka.ProxyBroker.subcmds.ServeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequestMapping("/")
@RestController
@EnableAutoConfiguration
public class SpringBootConsoleApplication {

    private final Logger LOG = LoggerFactory.getLogger(ServeCommand.class);

    private final ConcurrentHashMap<String, LookupResult> cache = new ConcurrentHashMap<>();
    private final ProxyCollector proxyProvider = new ProxyCollector(List.of(ProxyType.ALL), "");

    // https://stackoverflow.com/a/38668148
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationEvent() {

        // https://stackoverflow.com/a/28195667
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                try {
                    ArrayList<String> proxies = proxyProvider.getProxies(Arrays.asList(ProxyType.ALL));
                    ExecutorService executorService = Executors.newCachedThreadPool();

                    InputStream database = getClass().getClassLoader().getResourceAsStream("GeoLite2-Country.mmdb");
                    DatabaseReader dbReader = new DatabaseReader.Builder(database)
                                .build();
                    // Add more proxies
                    for (String proxy : proxies) {
                        ProxyThread proxyThread = new ProxyThread(dbReader, cache, proxy, Arrays.asList(ProxyType.ALL), "");
                        executorService.submit(proxyThread);
                    }
                    // Check current proxies & see if they are still alive
                    for (String proxy : cache.keySet()){
                        ProxyThread proxyThread = new ProxyThread(dbReader, cache, proxy, Arrays.asList(ProxyType.ALL), "");
                        executorService.submit(proxyThread);
                    }

                    executorService.shutdown();
                } catch(IOException e) {

                }

            }
            // 300000ms is 5 minutes
        },0,300000);
    }

    @RequestMapping("/")
    public String start(@RequestParam(name = "type", required = false, defaultValue = "") String type,
                        @RequestParam(name = "countries", required = false, defaultValue = "") String countries,
                        @RequestParam(name = "lvl", required = false, defaultValue = "High") String lvl,
                        @RequestParam(name = "limit", required = false, defaultValue = "20") String limit) {
        Gson gson = new Gson();
        List<LookupResult> proxies = new ArrayList<>();
        cache.values().forEach((LookupResult value) -> {
            proxies.add(value);
        });
        return gson.toJson(proxies);
    }

    @RequestMapping("/api")
    public String home(){
        return "Hello, this is my home.";
    }

}