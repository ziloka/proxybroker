package com.ziloka.ProxyBroker.services;

import com.ziloka.ProxyBroker.services.models.ProxySource;
import com.ziloka.ProxyBroker.services.models.ProxyType;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jctools.queues.MpscArrayQueue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProxyCollector {

  private final Logger LOG = LogManager.getLogger(ProxyCollector.class);

  private List<ProxyType> type;
  private String countries;
  private List<ProxySource> proxySources;

  /**
   * @param type      - Proxy Type
   * @param countries - Proxy must be from specified countries
   */
  public ProxyCollector(List<ProxyType> type, String countries) {
    this.type = type;
    this.countries = countries;
    this.setSources();
  }

  /**
   * Load proxy sources from resources/ProxySources.json file
   */
  public void setSources() {

    // https://mkyong.com/java/java-read-a-file-from-resources-folder/
    // https://attacomsian.com/blog/gson-read-json-file

    try {
      Gson gson = new Gson();
      InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ProxySources.json");
      InputStreamReader streamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
          StandardCharsets.UTF_8);
      ProxySource[] proxySources = gson.fromJson(streamReader, ProxySource[].class);
      this.proxySources = Arrays.stream(proxySources).collect(Collectors.toList());
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Proxy used to collect proxies from proxy sources
   * 
   * @return Proxy Syntax host:port
   */
  public String getProxyForMainThread() {
    String proxy = "";
    try {
      HttpClient client = HttpClient.newBuilder()
          .version(Version.HTTP_2)
          .followRedirects(Redirect.NORMAL)
          .connectTimeout(Duration.ofSeconds(20))
          .build();
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(
              "https://api.getproxylist.com/proxy?anonymity[]=high%20anonymity&allowsHttps=1?protocol[]=socks4"))
          .build();
      HttpResponse<String> res = client.send(request, BodyHandlers.ofString());
      if (res.statusCode() == 200) {
        JsonObject apiResponse = JsonParser.parseString(res.body()).getAsJsonObject();
        String proxyIp = apiResponse.get("ip").getAsString();
        int proxyPort = apiResponse.get("port").getAsInt();
        proxy = String.format("%s:%d", proxyIp, proxyPort);
      }
    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
    }
    return proxy;
  }

  public void getProxies(ThreadPoolExecutor threadPoolExecutor, MpscArrayQueue<ArrayList<String>> uncheckedProxiesQueue,
      List<ProxyType> proxyType) {

    /*
     * NullPointerException - x.type is null
     * ProxySource type property is invalid in resources/ProxySources.json
     */

    Supplier<List<String>> getSpecifiedProxySource = () -> proxySources.stream()
        .filter(x -> proxyType.stream().filter(e -> e.equals(x.type)).count() == 1)
        .map(x -> x.url)
        .collect(Collectors.toList());

    List<String> iterateProxiesList = proxyType.stream().filter(x -> x.toString().equals("ALL")).count() == 1
        ? proxySources.stream().map(x -> x.url).collect(Collectors.toList())
        : getSpecifiedProxySource.get();

    HttpClient client = HttpClient.newBuilder()
        .version(Version.HTTP_2)
        .followRedirects(Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(8))
        .build();

    Pattern optionNamePattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+");

    for (String proxySource : iterateProxiesList) {
      threadPoolExecutor.submit(new Runnable() {
        public void run() {
          ArrayList<String> result = new ArrayList<String>();
          try {
            HttpResponse<String> res = client.send(HttpRequest.newBuilder()
                .uri(URI.create(proxySource))
                .build(), BodyHandlers.ofString());
            if (res.statusCode() == 200) {
              Matcher matcher = optionNamePattern.matcher(res.body());
              while (matcher.find()) {
                result.add(matcher.group().trim());
              }
              LOG.debug(String.format("Found %d proxies using source: %s", result.size(), proxySource));
              uncheckedProxiesQueue.add(result);

              // LOG.debug(String.format("Found %d proxies using %d sources", result.size(),
              // iterateProxiesList.size()));
            } else {
              System.out.println("GET request not worked");
              System.out.println("Status Code" + res.statusCode());
            }
          } catch (InterruptedException | IOException e) {
            // if(!(e instanceof SSLException) && !e.getMessage().equals("Unrecognized SSL
            // message, plaintext connection?")){
            // e.printStackTrace();
            // }
          }
        }
      });
    }

  }

}
