package com.ziloka.services;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class ProxyCollectorService {

    String type;
    String countries;
    String lvl;
    JSONObject ProxySources;
    Logger logger = LogManager.getLogger(ProxyCollectorService.class);

    public ProxyCollectorService(String type, String countries, String lvl) {
        this.type = type;
        this.countries = countries;
        this.lvl = lvl;
    }

    public void setSources() {

        // https://howtodoinjava.com/java/io/read-file-from-resources-folder/

        ClassLoader classLoader = getClass().getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream("ProxySources.json")) {

            String result = IOUtils.toString(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
            JSONParser jsonParser = new JSONParser();
            Object object = jsonParser.parse(result);
            this.ProxySources = (JSONObject) object;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<String> getProxies(String proxyType) {

        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> allProxySources = new ArrayList<String>();
        for(Object proxySources: this.ProxySources.values()){
            JSONArray specificProxySource = (JSONArray) proxySources;
            for(Object uri: specificProxySource){
                String ProxySourceuri = (String) uri;
                allProxySources.add(ProxySourceuri);
            }
        }

        @SuppressWarnings("unchecked")
        ArrayList<String> proxySources = proxyType != null ? (ArrayList<String>) this.ProxySources.get(proxyType) : allProxySources;

        for (Object proxySource : proxySources) {
            String uri = (String) proxySource;
            int statusCode;
            try {
                logger.debug(String.format("Collecting proxies from %s", uri));
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                con.setRequestProperty("Accept-Encoding", "gzip");
                con.setReadTimeout(3000);
                con.setConnectTimeout(3000);
                con.connect();
                int responseCode = con.getResponseCode();
                statusCode = responseCode;
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    Reader reader = "gzip".equals(con.getContentEncoding()) ?
                            new InputStreamReader(new GZIPInputStream(con.getInputStream())) :
                            new InputStreamReader(con.getInputStream());
                    String html = "";
                    while (true) {
                        int ch = reader.read();
                        if (ch==-1) {
                            break;
                        }
                        html = html + (char) ch;
                    }

                    // https://stackoverflow.com/a/11637672
//                    Pattern optionNamePattern = Pattern.compile("\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b:\\d{2,5}\\n");
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
            } catch (IOException e) {
                if(!(e instanceof SocketTimeoutException)){
                    e.printStackTrace();
                }
            }

        }

        logger.debug(String.format("Found %d proxies using %d sources", result.size(), this.ProxySources.size()));

        return result;

    }

}
