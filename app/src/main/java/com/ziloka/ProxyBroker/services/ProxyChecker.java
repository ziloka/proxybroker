package com.ziloka.ProxyBroker.services;

import com.ziloka.ProxyBroker.services.models.LookupResult;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.maxmind.geoip2.DatabaseReader;
import com.ziloka.ProxyBroker.services.models.ProxyType;
import com.ziloka.ProxyBroker.utils.JSON;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Collect information if proxy is online or not
 */
public class ProxyChecker {

    DatabaseReader dbReader;
    final ConcurrentHashMap<String, LookupResult> onlineProxies;
    String externalIpAddr;
    List<ProxyType> proxyType;
    String host;
    Integer port;
    String address;
    /*
     * Elite Proxies (Level 1, High) - The server you connect to receive no information about your IP address
     * Anonymous Proxies (Level 2, Transparent) - server will recognize that a proxy is making the connection
     * Good use case for rotating open proxies
     * Transparent Proxies (Level 3, Transparent) Does not provide anonymity at all
     * Source: https://docs.proxymesh.com/article/78-proxy-anonymity-levels
     */
    String lvl;

    /**
     * ProxyChecker Constructor
     * @param dbReader  Database Reader for MaxMindDatabase
     * @param onlineProxies  Collection of online proxies
     * @param ipAddress  Ip Address
     * @param proxyType  Proxy Protocol (http, https, socks4, socks5)
     */
    public ProxyChecker(DatabaseReader dbReader, ConcurrentHashMap<String, LookupResult> onlineProxies, String externalIpAddr, String ipAddress, List<ProxyType> proxyType) {
        this.dbReader = dbReader;
        this.onlineProxies = onlineProxies;
        this.externalIpAddr = externalIpAddr;
        this.proxyType = proxyType;
        this.host = ipAddress.split(":")[0];
        this.port = Integer.parseInt(ipAddress.split(":")[1]);
        this.address = String.format("%s:%d", this.host, this.port);
    }

    /**
     * Checks if proxy is online or not
     * @return Boolean indicating if Proxy is online or not
     * @throws IOException Throws IOException
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
            if(res.statusCode() == HttpURLConnection.HTTP_OK){
                // Check for anonymity level
                JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
                String origin = json.get("origin").getAsString();
                if(JSON.isJsonValid(res.body())){
                    isOnline = true;
                    if(!origin.contains(externalIpAddr)){
                        this.lvl = "High";
                    } else {
                        this.lvl = "Low";
                    }
                }
            }
        } catch (Exception ignored) {}

        return isOnline;
    }

    /**
     * get proxy protocol
     * @return String
     */
    public ProxyType getProtocol(){
        ProxyType proxyType = null;
        List<Proxy.Type> protocols = Arrays.stream(Proxy.Type.values()).filter(x -> !x.name().equals("ALL")).collect(Collectors.toList());
        for(Proxy.Type protocol: protocols){
            try {
                System.out.println(protocol);
                URL url = new URL("http://httpbin.org/ip?json");
                InetSocketAddress proxyAddr = new InetSocketAddress(this.host, this.port);
                Proxy webProxy = new Proxy(protocol, proxyAddr);
                HttpURLConnection con = (HttpURLConnection) url.openConnection(webProxy);
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                int resCode = con.getResponseCode();
                System.out.printf("Status Code: %d\n", resCode);
                if(resCode == HttpURLConnection.HTTP_OK){
                    proxyType = ProxyType.valueOf(protocol.name());
                    break;
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        return proxyType;
    }

}
