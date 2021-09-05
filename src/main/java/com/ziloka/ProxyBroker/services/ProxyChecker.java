package com.ziloka.ProxyBroker.services;

import com.maxmind.geoip2.DatabaseReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;

public class ProxyChecker {

    private static final Logger logger = LogManager.getLogger(ProxyChecker.class);

    DatabaseReader dbReader;
    HashMap<String, LookupResult> onlineProxies;
    String proxyType;
    String address;
    String host;
    Integer port;

    public ProxyChecker(DatabaseReader dbReader, HashMap<String, LookupResult> onlineProxies, String ipAddress, String proxyType) {
        this.dbReader = dbReader;
        this.onlineProxies = onlineProxies;
        this.proxyType = proxyType;
        this.address = String.format("%s:%d", host, port);
        this.host = ipAddress.split(":")[0];
        this.port = Integer.parseInt(ipAddress.split(":")[1]);
    }

    public boolean check() throws IOException {

        // https://www.baeldung.com/java-connect-via-proxy-server
        // https://crunchify.com/how-to-run-multiple-threads-concurrently-in-java-executorservice-approach/

        boolean isOnline = false;
        try {
            HttpClient client = HttpClient.newBuilder()
                    .proxy(ProxySelector.of(new InetSocketAddress(this.host, this.port)))
                    .build();
            HttpRequest request = HttpRequest.newBuilder(
                    URI.create("http://httpbin.org/ip?json")
            ).build();
            HttpResponse<String> res = client.send(request, BodyHandlers.ofString());
            if(res.statusCode() == 200) isOnline = true;
        } catch (Exception ignored) {}

        return isOnline;
    }

}
