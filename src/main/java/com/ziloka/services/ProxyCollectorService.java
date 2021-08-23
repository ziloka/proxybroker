package com.ziloka.services;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

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
//            this.ProxySources = ProxySources.get(this.type.toLowerCase());

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<String> getProxies() {

        ArrayList<String> result = new ArrayList<String>();
        for (Object proxySource : this.ProxySources) {
            String uri = (String) proxySource;
            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        result.add(inputLine);
                    }

                    in.close();

                } else {
                    System.out.println("GET request not worked");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
        }

        return result;

    }

}
