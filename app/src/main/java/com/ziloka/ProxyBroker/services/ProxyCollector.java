package com.ziloka.ProxyBroker.services;

import com.ziloka.ProxyBroker.services.models.ProxySource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProxyCollector {

    /**
     * @param type - Proxy Type
     * @param countries - Proxy must be from specified countries
     */
    public ProxyCollector() {}

    /**
     * Load proxy sources from resources/ProxySources.json file
     */
    public static List<ProxySource> getSources() {

        // https://mkyong.com/java/java-read-a-file-from-resources-folder/
        // https://attacomsian.com/blog/gson-read-json-file

        List<ProxySource> result = null;

        try {
            Gson gson = new Gson();
            InputStream inputStream = ProxyCollector.class.getClassLoader().getResourceAsStream("ProxySources.json");
            InputStreamReader streamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
            ProxySource[] proxySources = gson.fromJson(streamReader, ProxySource[].class);
            result = Arrays.stream(proxySources).collect(Collectors.toList());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Proxy used to collect proxies from proxy sources
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
                    .uri(URI.create("https://api.getproxylist.com/proxy?anonymity[]=high%20anonymity&allowsHttps=1?protocol[]=socks4"))
                    .build();
            HttpResponse<String> res = client.send(request, BodyHandlers.ofString());
            if(res.statusCode() == 200){
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

    public static ArrayList<String> getProxies(String proxySource) {

        ArrayList<String> result = new ArrayList<>();

        HttpClient client = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(8))
                .build();

            int statusCode;
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(proxySource))
                        .build();
                HttpResponse<String> res = client.send(request, BodyHandlers.ofString());
                statusCode = res.statusCode();
                if (res.statusCode() == 200) {
                    String html = res.body();
                    Pattern optionNamePattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+");
                    Matcher matcher = optionNamePattern.matcher(html);
                    while(matcher.find()){
                        result.add(matcher.group().trim());
                    }
                } else {
                    System.out.println("GET request not worked");
                    System.out.println("Status Code"+ statusCode);
                }
            } catch (InterruptedException | IOException e) {
//                if(!(e instanceof SSLException) && !e.getMessage().equals("Unrecognized SSL message, plaintext connection?")){
//                    e.printStackTrace();
//                }
            }

        return result;

    }

}
