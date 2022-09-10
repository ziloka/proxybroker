package com.ziloka.ProxyBroker.subcmds;

import com.ziloka.ProxyBroker.services.ProxyCollector;
import com.ziloka.ProxyBroker.services.models.ProxyType;
import com.ziloka.ProxyBroker.subcmds.converters.IProxyTypeConverter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jctools.queues.MpscArrayQueue;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Command(name = "grab")
public class GrabCommand implements Runnable {

  private final Logger LOG = LogManager.getLogger(GrabCommand.class);

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

  @Option(names = { "--timeout", "-t" }, defaultValue = "8", type = Integer.class)
  private int timeout;

  @Option(names = { "--outfile", "-o" }, defaultValue = "proxies.txt")
  private String OutFile;

  @Option(names = { "--verbose", "-v" }, defaultValue = "false", type = Boolean.class)
  private boolean isVerbose;

  @Override
  public void run() {

    if (isVerbose)
      Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
    else
      Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.OFF);

    ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    MpscArrayQueue<ArrayList<String>> uncheckedProxies = new MpscArrayQueue<ArrayList<String>>(100);

    LOG.debug("Collecting proxies");

    ProxyCollector proxyProvider = new ProxyCollector(types, countries);
    proxyProvider.getProxies(threadPoolExecutor, uncheckedProxies, types);

    while (true) {
      uncheckedProxies.drain((proxies) -> {
        try {
          BufferedWriter writer = new BufferedWriter(new FileWriter(OutFile));
          writer.write(String.join("\n", proxies));
          writer.close();
          System.exit(0);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }
  }

}
