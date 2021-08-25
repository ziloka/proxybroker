package com.ziloka.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;

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
        // Source of NumberFormatException
        int port = Integer.parseInt(ipAddress.substring(ipAddress.indexOf(":") + 1));
        URL url = new URL("http://httpbin.org/ip?json");

        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
            Proxy webProxy = new Proxy(Proxy.Type.HTTP, inetSocketAddress);
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) url.openConnection(webProxy);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setReadTimeout(3000);
            con.setConnectTimeout(3000);
            con.connect();
            long startTime = System.nanoTime();
            int responseCode = con.getResponseCode();
            long endTime = System.nanoTime();
//            logger.debug(String.format("Get Request took %dms", (endTime-startTime)/1000000));
            if (responseCode == HttpURLConnection.HTTP_OK) {
                isOnline = true;
            }
            else{
//                System.out.println("***in ProxyCheckerService, isOnline=false, " +ipAddress);
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }

        return isOnline;
    }

}
