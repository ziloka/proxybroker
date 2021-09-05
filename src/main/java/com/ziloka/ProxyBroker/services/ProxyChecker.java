package com.ziloka.ProxyBroker.services;

import com.maxmind.geoip2.DatabaseReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;

/**
 * ProxyChecker class
 * Collection information if proxy is online or not
 */
public class ProxyChecker {

    private static final Logger logger = LogManager.getLogger(ProxyChecker.class);

    DatabaseReader dbReader;
    HashMap<String, LookupResult> onlineProxies;
    String proxyType;
    String address;
    String host;
    Integer port;
    /*
     * Elite Proxies (Level 1) - The server you connect to receive no information about your IP address
     * Anonymous Proxies (Level 2) - server will recognize that a proxy is making the connection
     * Good use case for rotating open proxies
     * Transpartant Proxies (Level 3) Does not provide anonymity at all
     * Source: https://docs.proxymesh.com/article/78-proxy-anonymity-levels
     */
    String lvl;

    /**
     * ProxyChecker Constructor
     * @param dbReader - Database Reader for MaxMindDatabase
     * @param onlineProxies - Collection of online proxies
     * @param ipAddress - Ip Address
     * @param proxyType - Proxy Protocol (http, https, socks4, socks5)
     */
    public ProxyChecker(DatabaseReader dbReader, HashMap<String, LookupResult> onlineProxies, String ipAddress, String proxyType) {
        this.dbReader = dbReader;
        this.onlineProxies = onlineProxies;
        this.proxyType = proxyType;
        this.address = String.format("%s:%d", host, port);
        this.host = ipAddress.split(":")[0];
        this.port = Integer.parseInt(ipAddress.split(":")[1]);
    }

    /**
     * Checks if proxy is online or not
     * @return Boolean indicating if Proxy is online or not
     * @throws IOException
     */
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

    /**
     * Checks proxy protocol is not specified
     * @return String
     */
    public String checkProtocol(){
        String proxyType = null;
        for(Proxy.Type protocol: Proxy.Type.values()){
            try {
                Proxy proxy = new Proxy(protocol, new InetSocketAddress(this.host, this.port));
                HttpURLConnection con = (HttpURLConnection) (new URL("http://httpbin.org/ip?json")).openConnection(proxy);
                con.setReadTimeout(8000);
                con.setConnectTimeout(8000);
                con.connect();
                int resCode = con.getResponseCode();
                if(resCode == 200) proxyType = String.valueOf(protocol);
                else continue;
            } catch(IOException e){
                e.printStackTrace();
            }
        }

        return proxyType;
    }

}
