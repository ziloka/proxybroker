package com.ziloka.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.Function;
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

        // https://mkyong.com/java/java-read-a-file-from-resources-folder/

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("ProxySources.json");
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            String json = "";
            String line;
            while((line = reader.readLine()) != null){
                json = json+line;
            }
            this.ProxySources = new JSONObject(json);

        } catch(IOException e){
            e.printStackTrace();
        }

    }

    public String getProxyForMainThread() {
        String proxy = "";
        int statusCode;
        try {
            URL url = new URL("https://api.getproxylist.com/proxy?anonymity[]=high%20anonymity&allowsHttps=1?protocol[]=socks4");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Encoding", "gzip");
            con.setReadTimeout(8000);
            con.setConnectTimeout(8000);
            con.connect();
            int responseCode = con.getResponseCode();
            statusCode = responseCode;
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                Reader reader = "gzip".equals(con.getContentEncoding()) ?
                        new InputStreamReader(new GZIPInputStream(con.getInputStream())) :
                        new InputStreamReader(con.getInputStream());
                String json = "";
                while (true) {
                    int ch = reader.read();
                    if (ch==-1) {
                        break;
                    }
                    json = json + (char) ch;
                }

                JSONObject apiRes = new JSONObject(json);
                String proxyIp = (String) apiRes.get("ip");
                int proxyPort = (int) apiRes.get("port");
                proxy = String.format("%s:%d", proxyIp, proxyPort);
            } else {
                System.out.println("GET request not worked");
                System.out.println("Status Code"+ statusCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return proxy;
    }

    public ArrayList<String> getProxies(String proxyType) {

        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> allProxySources = new ArrayList<String>();
        for(Object entry: this.ProxySources.keySet()){
            JSONArray values = (JSONArray) this.ProxySources.get((String) entry);
            for(Object uri: values){
                allProxySources.add((String) uri);
            }
        }

        Function<String, ArrayList<String>> getSpecifiedProxySource = (String specificProxyType) -> {
            ArrayList<String> specifiedProxySource = new ArrayList<String>();
            JSONArray specificProxySource = (JSONArray) this.ProxySources.get(specificProxyType);
            for (Object entry: specificProxySource) specifiedProxySource.add((String) entry);
            return specifiedProxySource;
        };

        @SuppressWarnings("unchecked")
        ArrayList<String> iterateProxiesList = proxyType == "" ? allProxySources : getSpecifiedProxySource.apply(proxyType);

        for (Object proxySource : iterateProxiesList) {
            String uri = (String) proxySource;
            int statusCode;
            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                con.setRequestProperty("Accept-Encoding", "gzip");
                con.setReadTimeout(8000);
                con.setConnectTimeout(8000);
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
                if(!(e instanceof SocketTimeoutException) && !(e instanceof SSLException)){
                    e.printStackTrace();
                }
            }

        }

        logger.debug(String.format("Found %d proxies using %d sources", result.size(), iterateProxiesList.size()));

        return result;

    }

}
