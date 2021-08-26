package com.ziloka.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class ProxyCheckerService {

    Logger logger = LogManager.getLogger(ProxyCollectorService.class);

    public ProxyCheckerService() {

    }

    // str syntax host:port
    public boolean check(String ipAddress) throws IOException {

        // https://www.baeldung.com/java-connect-via-proxy-server
        // https://crunchify.com/how-to-run-multiple-threads-concurrently-in-java-executorservice-approach/

        boolean isOnline = false;
        String host = ipAddress.substring(0, ipAddress.indexOf(":"));
        int port = Integer.parseInt(ipAddress.substring(ipAddress.indexOf(":") + 1));
        URL url = new URL("http://httpbin.org/ip?json");

        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
            Proxy webProxy = new Proxy(Proxy.Type.HTTP, inetSocketAddress);
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) url.openConnection(webProxy);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setReadTimeout(10000);
            con.setConnectTimeout(10000);
            con.connect();
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                isOnline = true;
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }

        return isOnline;
    }

}
