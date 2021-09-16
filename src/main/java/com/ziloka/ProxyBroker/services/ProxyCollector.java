package com.ziloka.ProxyBroker.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ziloka.ProxyBroker.services.models.ProxySource;
import com.ziloka.ProxyBroker.services.models.ProxyType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProxyCollector {

    private static final Logger logger = LogManager.getLogger(ProxyCollector.class);

    String type;
    String countries;
    List<ProxySource> proxySources;

    /**
     * @param type - Proxy Type
     * @param countries - Proxy must be from specified countries
     */
    public ProxyCollector(String type, String countries) throws IOException {
        this.type = type;
        this.countries = countries;
        this.setSources();
    }

    /**
     * Load proxy sources from resources/ProxySources.json file
     */
    public void setSources() throws IOException {

        // https://mkyong.com/java/java-read-a-file-from-resources-folder/
        // https://attacomsian.com/blog/gson-read-json-file

        try {
            Gson gson = new Gson();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ProxySources.json");
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            String json = "";
            String line;
            while ((line = reader.readLine()) != null) {
                json = json + line;
            }
            ProxySource[] proxySources = gson.fromJson(json.replaceAll("\\s+", ""), ProxySource[].class);
            this.proxySources = Arrays.stream(proxySources).toList();
        } catch(IOException e) {
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

    public ArrayList<String> getProxies(ProxyType proxyType) {

        ArrayList<String> result = new ArrayList<>();

        /*
         * NullPointerException - x.type is null
         * ProxySource type property is invalid in resources/ProxySources.json
         */
        Function<ProxyType, ArrayList<String>> getSpecifiedProxySource = (ProxyType specificProxyType) -> (ArrayList<String>) proxySources.stream()
                .filter(x -> x.type.equals(proxyType))
                .map(x -> x.url)
                .collect(Collectors.toList());

        ArrayList<String> iterateProxiesList = getSpecifiedProxySource.apply(proxyType);

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
