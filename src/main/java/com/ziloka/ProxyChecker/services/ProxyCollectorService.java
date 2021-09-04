package com.ziloka.ProxyChecker.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProxyCollectorService {

    String type;
    String countries;
    String lvl;
    List<ProxySource> proxySources;
    private static final Logger logger = LogManager.getLogger(ProxyCollectorService.class);

    public ProxyCollectorService(String type, String countries, String lvl) {
        this.type = type;
        this.countries = countries;
        this.lvl = lvl;
    }

    public void setSource() {

        // https://mkyong.com/java/java-read-a-file-from-resources-folder/
        // https://attacomsian.com/blog/gson-read-json-file

        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get(ClassLoader.getSystemResource("ProxySources.json").toURI()));
            ProxySource[] proxySources = gson.fromJson(reader, ProxySource[].class);
            reader.close();
            this.proxySources = Arrays.stream(proxySources).toList();

        } catch(IOException | URISyntaxException e){
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

    public ArrayList<String> getProxies(ProxyType proxyType) {

        ArrayList<String> result = new ArrayList<>();

        Function<ProxyType, ArrayList<String>> getSpecifiedProxySource = (ProxyType specificProxyType) -> {
            return (ArrayList<String>) proxySources.stream()
                    /*
                     NullPointerException - x.type is null
                     ProxySource type property is invalid in resources/ProxySources.json
                    */
                    .filter(x -> x.type.equals(proxyType))
                    .map(x -> x.url)
                    .collect(Collectors.toList());
        };

        Supplier<ArrayList<String>> getEntireProxyList = () -> {
            return (ArrayList<String>) proxySources.stream().map(x -> x.url).collect(Collectors.toList());
        };

        ArrayList<String> iterateProxiesList = proxyType.equals("") ?
                getEntireProxyList.get() :
                getSpecifiedProxySource.apply(proxyType);

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
