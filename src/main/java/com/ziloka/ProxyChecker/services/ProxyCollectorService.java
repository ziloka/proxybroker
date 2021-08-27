package com.ziloka.ProxyChecker.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
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
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyCollectorService {

    String type;
    String countries;
    String lvl;
    JSONObject ProxySources;
    private static final Logger logger = LogManager.getLogger(ProxyCollectorService.class);

    public ProxyCollectorService(String type, String countries, String lvl) {
        this.type = type;
        this.countries = countries;
        this.lvl = lvl;
    }

    public void setSources() {

        // https://mkyong.com/java/java-read-a-file-from-resources-folder/

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("ProxySources.json");
            if(inputStream == null) throw new Error("resources/ProxySources.json is missing");
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder json = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                json.append(line);
            }
            this.ProxySources = new JSONObject(json.toString());

        } catch(IOException e){
            e.printStackTrace();
        }

    }

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
                JSONObject apiResponse = new JSONObject(res.body());
                String proxyIp = (String) apiResponse.get("ip");
                int proxyPort = (int) apiResponse.get("port");
                proxy = String.format("%s:%d", proxyIp, proxyPort);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return proxy;
    }

    public ArrayList<String> getProxies(String proxyType) {

        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> allProxySources = new ArrayList<>();
        for(String entry: this.ProxySources.keySet()){
            JSONArray values = (JSONArray) this.ProxySources.get(entry);
            for(Object uri: values){
                allProxySources.add((String) uri);
            }
        }

        Function<String, ArrayList<String>> getSpecifiedProxySource = (String specificProxyType) -> {
            ArrayList<String> specifiedProxySource = new ArrayList<>();
            JSONArray specificProxySource = (JSONArray) this.ProxySources.get(specificProxyType);
            for (Object entry: specificProxySource) specifiedProxySource.add((String) entry);
            return specifiedProxySource;
        };

        ArrayList<String> iterateProxiesList = proxyType.equals("") ? allProxySources : getSpecifiedProxySource.apply(proxyType);

        HttpClient client = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
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
                    logger.debug(String.format("Found %d proxies using source: %s", count, proxySource));
                } else {
                    System.out.println("GET request not worked");
                    System.out.println("Status Code"+ statusCode);
                }
            } catch (InterruptedException | IOException e) {
                if(!(e instanceof SSLException)){
                    e.printStackTrace();
                }
            }

        }

        logger.debug(String.format("Found %d proxies using %d sources", result.size(), iterateProxiesList.size()));

        return result;

    }

}
