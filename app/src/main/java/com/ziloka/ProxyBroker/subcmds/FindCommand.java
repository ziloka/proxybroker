package com.ziloka.ProxyBroker.subcmds;

import com.google.gson.JsonParser;
import com.maxmind.geoip2.DatabaseReader;
import com.ziloka.ProxyBroker.services.ProxyChecker;
import com.ziloka.ProxyBroker.services.ProxyCollector;
import com.ziloka.ProxyBroker.services.models.LookupResult;
import com.ziloka.ProxyBroker.services.models.ProxyType;
import com.ziloka.ProxyBroker.subcmds.converters.IProxyTypeConverter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jctools.queues.MpscArrayQueue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Callable;

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
  @Option(names = { "--limit", "-l" }, defaultValue = "10", type = Integer.class)
  private int limit;

  @Option(names = { "--timeout", "-t" }, defaultValue = "1", type = Integer.class)
  private int timeout;

  @Option(names = { "--outfile", "-o" }, defaultValue = "")
  private String OutFile;

  @Option(names = { "--verbose", "-v" }, defaultValue = "false", type = Boolean.class)
  private boolean isVerbose;

  /**
   * Executes when user runs "proxybroker find"
   */
  @Override
  public Integer call() {

    try {

      Configurator.setAllLevels(LogManager.getRootLogger().getName(), isVerbose ? Level.DEBUG : Level.OFF);

      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder(
          URI.create("http://httpbin.org/ip?json")).build();
      HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());
      // current ip address
      String externalIpAddr = JsonParser.parseString(res.body()).getAsJsonObject().get("origin").getAsString();

      ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
      MpscArrayQueue<ArrayList<String>> uncheckedProxiesQueue = new MpscArrayQueue<ArrayList<String>>(100);
      MpscArrayQueue<LookupResult> checkedProxiesQueue = new MpscArrayQueue<LookupResult>(100);

      ProxyCollector proxyProvider = new ProxyCollector(types, countries);
      proxyProvider.getProxies(threadPoolExecutor, uncheckedProxiesQueue, types);

      InputStream database = getClass().getClassLoader().getResourceAsStream("GeoLite2-Country.mmdb");
      DatabaseReader dbReader = new DatabaseReader.Builder(database)
          .build();

      AtomicInteger counter = new AtomicInteger();
      while (true) {
        // https://www.geeksforgeeks.org/java-8-consumer-interface-in-java-with-examples/
        uncheckedProxiesQueue.drain((proxies) -> {
          for (String s : proxies) {
            try {
              ProxyChecker.check(threadPoolExecutor, checkedProxiesQueue, dbReader, externalIpAddr, s, types);
            } catch (IOException e) {
              e.printStackTrace();
            }
            
          }
        });

        checkedProxiesQueue.drain((lookupResult) -> {
          if (OutFile.length() != 0) {
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(OutFile, true)))) {
              writer.println(String.format("%s:%d", lookupResult.getProxyHost(), lookupResult.getProxyPort()));
              writer.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          } else {
            System.out.printf("<Proxy %s:%d %s>\n", lookupResult.getProxyHost(), lookupResult.getProxyPort(),
                lookupResult.getIsoCode());
          }

          counter.addAndGet(1);
          // how to exit program
          if (counter.get() >= limit) {
            System.exit(0);
          }

        });
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return 0;

  }

}