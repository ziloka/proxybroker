package com.ziloka.ProxyBroker.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ziloka.ProxyBroker.services.models.ProxySource;
import com.ziloka.ProxyBroker.services.models.ProxyType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
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
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProxyCollector {

    private final Logger LOG = LogManager.getLogger(ProxyCollector.class);

    List<ProxyType> type;
    String countries;
    List<ProxySource> proxySources;

    /**
     * @param type - Proxy Type
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
            InputStreamReader streamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
            ProxySource[] proxySources = gson.fromJson(streamReader, ProxySource[].class);
            this.proxySources = Arrays.stream(proxySources).collect(Collectors.toList());
        } catch(Exception e) {
            e.printStackTrace();
        }

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

    public ArrayList<String> getProxies(List<ProxyType> proxyType) {

        ArrayList<String> result = new ArrayList<>();

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

        for (String proxySource : iterateProxiesList) {
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
                    int count = 0;
                    while(matcher.find()){
                        count++;
                        result.add(matcher.group().trim());
                    }
                    LOG.debug(String.format("Found %d proxies using source: %s", count, proxySource));
                } else {
                    System.out.println("GET request not worked");
                    System.out.println("Status Code"+ statusCode);
                }
            } catch (InterruptedException | IOException e) {
                if(!(e instanceof SSLException) && !e.getMessage().equals("Unrecognized SSL message, plaintext connection?")){
                    e.printStackTrace();
                }
            }

        }

        LOG.debug(String.format("Found %d proxies using %d sources", result.size(), iterateProxiesList.size()));

        return result;

    }

}
