package com.ziloka.ProxyBroker.services;

import com.ziloka.ProxyBroker.services.models.LookupResult;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import com.ziloka.ProxyBroker.services.models.ProxyType;
import com.ziloka.ProxyBroker.utils.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jctools.queues.MpscArrayQueue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Collect information if proxy is online or not
 */
public class ProxyChecker {

  private final Logger LOG = LogManager.getLogger(ProxyChecker.class);

  /**
   * Checks if proxy is online or not
   * 
   * @return Boolean indicating if Proxy is online or not
   * @throws IOException Throws IOException
   */
  public static void check(ThreadPoolExecutor threadPoolExecutor, MpscArrayQueue<LookupResult> checkedProxiesQueue,
  DatabaseReader dbReader, String externalIpAddr,
  String ipAddress, List<ProxyType> proxyType) throws IOException {

    threadPoolExecutor.submit(new Runnable() {
      public void run() {

        String host = ipAddress.split(":")[0];
        Integer port = Integer.parseInt(ipAddress.split(":")[1]);

        // https://www.baeldung.com/java-connect-via-proxy-server
        // https://crunchify.com/how-to-run-multiple-threads-concurrently-in-java-executorservice-approach/
        boolean isOnline = false;
        try {
          HttpClient client = HttpClient.newBuilder()
              .proxy(ProxySelector.of(new InetSocketAddress(host, port)))
              .build();
          HttpRequest request = HttpRequest.newBuilder(
              URI.create("http://httpbin.org/ip?json")).build();
          HttpResponse<String> res = client.send(request, BodyHandlers.ofString());
          if (res.statusCode() == HttpURLConnection.HTTP_OK) {
            // Check for anonymity level
            JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
            String origin = json.get("origin").getAsString();
            if (JSON.isJsonValid(res.body())) {
              isOnline = true;
              if (!origin.contains(externalIpAddr)) {
                // lvl = "High";
              } else {
                // lvl = "Low";
              }
            }
          }
          InetAddress ipAddress = InetAddress.getByName(host);
          CountryResponse response = dbReader.country(ipAddress);
          Country country = response.getCountry();
      
          checkedProxiesQueue.add(new LookupResult(host, port, country.getIsoCode(), country.getName()));
        } catch (Exception ignored) {

        }
      }
    });

  }

}
