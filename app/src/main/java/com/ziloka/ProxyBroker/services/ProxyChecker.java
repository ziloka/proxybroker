package com.ziloka.ProxyBroker.services;

import com.ziloka.ProxyBroker.services.models.LookupResult;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.maxmind.geoip2.DatabaseReader;
import com.ziloka.ProxyBroker.services.models.ProxyType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collect information if proxy is online or not
 */
public class ProxyChecker {

    private final Logger LOG = LogManager.getLogger(ProxyChecker.class);

    DatabaseReader dbReader;
    ConcurrentHashMap<String, LookupResult> onlineProxies;
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
    public ProxyChecker(DatabaseReader dbReader, ConcurrentHashMap<String, LookupResult> onlineProxies, String ipAddress, List<ProxyType> proxyType) {
        this.dbReader = dbReader;
        this.onlineProxies = onlineProxies;
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
            if(res.statusCode() == 200){
                isOnline = true;
                // Check for anonymity level
                JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
                String origin = json.get("origin").getAsString();
                Pattern pattern = Pattern.compile("(\\d+\\.)+\\d+,\\s(\\d+\\.)+\\d+", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(origin);
                if(!matcher.find()){
                    // Level 1, Elite Anonymity
                    this.lvl = "High";
                } else {
                    // There are two ip addresses, most likely transparent or anonymous proxy
                    this.lvl = "Low";
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
        for(Proxy.Type protocol: Proxy.Type.values()){
            try {
                Proxy proxy = new Proxy(protocol, new InetSocketAddress(this.host, this.port));
                HttpURLConnection con = (HttpURLConnection) (new URL("http://httpbin.org/ip?json")).openConnection(proxy);
                con.setReadTimeout(8000);
                con.setConnectTimeout(8000);
                con.connect();
                int resCode = con.getResponseCode();
                if(resCode == 200) proxyType = ProxyType.valueOf(protocol.name());
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        return proxyType;
    }

}
