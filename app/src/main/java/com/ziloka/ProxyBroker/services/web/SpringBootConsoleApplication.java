package com.ziloka.ProxyBroker.services.web;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.maxmind.geoip2.DatabaseReader;
import com.ziloka.ProxyBroker.services.ProxyCollector;
import com.ziloka.ProxyBroker.services.models.LookupResult;

import com.ziloka.ProxyBroker.services.models.ProxyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.jctools.queues.MpscArrayQueue;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;;

@Component
@RequestMapping("/")
@RestController
@EnableAutoConfiguration
public class SpringBootConsoleApplication {

  private final Logger LOG = LoggerFactory.getLogger(SpringBootConsoleApplication.class);

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
          ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
          MpscArrayQueue<ArrayList<String>> uncheckedProxiesQueue = new MpscArrayQueue<ArrayList<String>>(100);
          proxyProvider.getProxies(threadPoolExecutor, uncheckedProxiesQueue, List.of(ProxyType.ALL));
          ExecutorService executorService = Executors.newCachedThreadPool();

          HttpClient client = HttpClient.newHttpClient();
          HttpRequest request = HttpRequest.newBuilder(
              URI.create("http://httpbin.org/ip?json")).build();
          HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());
          String externalIpAddr = JsonParser.parseString(res.body()).getAsJsonObject().get("origin").getAsString();

          InputStream database = getClass().getClassLoader().getResourceAsStream("GeoLite2-Country.mmdb");
          DatabaseReader dbReader = new DatabaseReader.Builder(database)
              .build();

          // proxies.addAll(cache.keySet());
          // Add more proxies & Check current proxies & see if they are still alive
          // for (String proxy : proxies) {
          //   ProxyThread proxyThread = new ProxyThread(dbReader, cache, externalIpAddr, proxy, List.of(ProxyType.ALL),
          //       "High");
          //   executorService.submit(proxyThread);
          // }

          executorService.shutdown();
        } catch (IOException | InterruptedException e) {

        }

      }
      // 300000ms is 5 minutes
    }, 0, 300000);
  }

  @RequestMapping("/")
  public String start(@RequestParam(name = "type", required = false, defaultValue = "") String type,
      @RequestParam(name = "countries", required = false, defaultValue = "") String countries,
      @RequestParam(name = "lvl", required = false, defaultValue = "High") String lvl,
      @RequestParam(name = "limit", required = false, defaultValue = "20") String limit) {
    Gson gson = new Gson();
    List<LookupResult> proxies = new ArrayList<>(cache.values());
    return gson.toJson(proxies);
  }

  // http://localhost:8080/request?url=https%3A%2F%2Fwww.google.com%2Fsearch%3Fq%3Durl%2Bonline%2Bbuilder
  // http://localhost:8080/request?url=http%3A%2F%2Fhttpbin.org%2Fip
  @RequestMapping("/request")
  public String home(@RequestParam(name = "url", required = true) String url) {
    long requestStart = System.currentTimeMillis();
    String result = null;
    Supplier<HttpResponse<String>> requestFunc = () -> {
      // https://stackoverflow.com/a/37180410
      List<String> list = new ArrayList<>(cache.keySet());

      int randNextInt = 1;
      if (list.size() > 0) {
        while (randNextInt <= 1) {
          // https://www.baeldung.com/java-random-list-element
          Random rand = new Random();
          randNextInt = rand.nextInt(list.size());
        }
      }

      String randomKey = list.get(randNextInt);

      String host = randomKey.split(":")[0];
      LOG.info(randomKey);
      int port = Integer.parseInt(randomKey.split(":")[1]);
      HttpClient client = HttpClient.newBuilder()
          .proxy(ProxySelector.of(new InetSocketAddress(host, port)))
          .connectTimeout(Duration.ofSeconds(1))
          .build();
      HttpRequest request = HttpRequest.newBuilder(
          URI.create(url)).build();
      HttpResponse<String> res = null;
      try {
        res = client.send(request, HttpResponse.BodyHandlers.ofString());
      } catch (IOException | InterruptedException e) {
        if (e instanceof IOException) {
          LOG.error(e.getMessage());
          return null;
        } else {
          e.printStackTrace();
        }
      }

      return res;
    };

    while (result == null) {
      int responseCount = 1;
      HttpResponse<String> response = requestFunc.get();
      while (response == null) {
        responseCount += 1;
        response = requestFunc.get();
      }
      System.out.printf("Made %d requests, There are %d active proxies, time taken: %dms\n", responseCount,
          cache.size(), System.currentTimeMillis() - requestStart);
      result = response.body();
    }

    return result;
  }

}