package com.ziloka.ProxyBroker.services;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.ziloka.ProxyBroker.services.models.LookupResult;
import com.ziloka.ProxyBroker.services.models.ProxySource;
import com.ziloka.ProxyBroker.services.models.ProxyType;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// https://www.geeksforgeeks.org/producer-consumer-solution-using-threads-java/
// linked lists
public class ProxyService {

    private final List<ProxySource> proxySources = ProxyCollector.getSources();
    private final LinkedList<ArrayList<String>> list = new LinkedList<ArrayList<String>>();
    private final int capacity = 2;
    private final DatabaseReader dbReader;
    private final ConcurrentHashMap<String, LookupResult> onlineProxies;
    private final String externalIpAddr;
    private final List<ProxyType> types;
    private final String lvl;
    private final String countries;
    private final ProxyLookup proxyLookup;
    private final int limit;

    public ProxyService(DatabaseReader dbReader, ConcurrentHashMap<String, LookupResult> onlineProxies, String externalIpAddr, List<ProxyType> types, String lvl, String countries, int limit) {
        this.dbReader = dbReader;
        this.onlineProxies = onlineProxies;
        this.externalIpAddr = externalIpAddr;
        this.types = types;
        this.lvl = lvl;
        this.countries = countries;
        this.proxyLookup = new ProxyLookup(dbReader);
        this.limit = limit;
    }

    // do task #1
    public void produce() throws InterruptedException {
        while (true) {
            synchronized (this){
                while (list.size() == capacity) wait();

                ProxyCollector proxyProvider = new ProxyCollector(types, countries);

                Supplier<List<String>> getSpecifiedProxySource = () -> proxySources.stream()
                        .filter(x -> proxySources.stream().filter(e -> e.equals(x)).count() == 1)
                        .map(x -> x.url)
                        .collect(Collectors.toList());

                List<String> iterateProxiesList = types.stream().filter(x -> x.toString().equals("ALL")).count() == 1
                        ? proxySources.stream().map(x -> x.url).collect(Collectors.toList())
                        : getSpecifiedProxySource.get();

                HttpClient client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_2)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(8))
                        .build();

                for(String proxySource : iterateProxiesList) {
                    ArrayList<String> proxies = proxyProvider.getProxies(proxySource);
                    list.add(proxies);

                    // notify consumer thread that it can start consuming
                    notify();
                }
            }
            break;
        }
    }

    // do task #2
    public void consume() throws InterruptedException, IOException, GeoIp2Exception {

        // https://www.baeldung.com/java-future#more-multithreading-with-thread-pools
        ExecutorService executorService = Executors.newCachedThreadPool();

        while (true) {
          // https://stackoverflow.com/questions/13578855/get-memory-used-by-a-process-java/13578901
          // https://stackoverflow.com/a/13578901
          System.out.printf("Freed memory: %dmb\n", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed()/1024/1024);
            synchronized (this)
            {
                // consumer thread waits while list
                // is empty
                while (list.size() == 0)
                    wait();

                // to retrieve the first job in the list
                ArrayList<String> proxies = list.removeFirst();

//                System.out.println(proxies);
                // Business logic
                for(String proxy : proxies) {
                    executorService.submit(new ProxyThread(dbReader, onlineProxies, externalIpAddr, proxy, types, lvl));
                }

                // Wake up producer thread
                notify();
            }
            if (onlineProxies.size() <= limit) {
                break;
            }
        }
        return;
    }

}
