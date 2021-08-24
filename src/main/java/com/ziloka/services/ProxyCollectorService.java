package com.ziloka.services;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
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
    int limit;
    JSONArray ProxySources;
//    ArrayList<Object> ProxySources;

    public ProxyCollectorService(String type, String countries, String lvl, int limit) {
        this.type = type;
        this.countries = countries;
        this.lvl = lvl;
        this.limit = limit;
    }

    public void setSources() {

        // https://howtodoinjava.com/java/io/read-file-from-resources-folder/

        ClassLoader classLoader = getClass().getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream("ProxySources.json")) {

            String result = IOUtils.toString(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
            JSONParser jsonParser = new JSONParser();
            Object object = jsonParser.parse(result);
            JSONObject ProxySources = (JSONObject) object;
            this.ProxySources = (JSONArray) ProxySources.get(this.type.toLowerCase());

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<String> getProxies() {

        ArrayList<String> result = new ArrayList<String>();
        for (Object proxySource : this.ProxySources) {
            String uri = (String) proxySource;
            int statusCode;
            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                con.setRequestProperty("Accept-Encoding", "gzip");
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

                    // https://stackoverflow.com/questions/11637555/regular-expressions-for-proxy-pattern
                    Pattern optionNamePattern = Pattern.compile("\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b:\\d{2,5}\n");
                    Matcher matcher = optionNamePattern.matcher(html);
                    while(matcher.find()){
                        result.add(matcher.group());
                    }
                } else {
                    System.out.println("GET request not worked");
                    System.out.println("Status Code"+ statusCode);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
        }

        return result;

    }

}
