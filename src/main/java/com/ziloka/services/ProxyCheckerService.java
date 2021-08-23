package com.ziloka.services;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;

public class ProxyCheckerService {

    public ProxyCheckerService() {

    }

    // str syntax host:port
    public boolean check(String ipAddress) throws IOException {

        // https://www.baeldung.com/java-connect-via-proxy-server
        // https://crunchify.com/how-to-run-multiple-threads-concurrently-in-java-executorservice-approach/

//        boolean isOnline = false;
//
//        String host = ipAddress.substring(0, ipAddress.indexOf(":"));
//        int port = Integer.parseInt(ipAddress.substring(ipAddress.indexOf(":") + 1));
//        URL url = new URL("http://httpbin.org/ip?json");
//
//        return isOnline;

        boolean isOnline = false;
        String host = ipAddress.substring(0, ipAddress.indexOf(":"));
        int port = Integer.parseInt(ipAddress.substring(ipAddress.indexOf(":") + 1));
        URL url = new URL("http://httpbin.org/ip?json");

        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
            Proxy webProxy = new Proxy(Proxy.Type.HTTP, inetSocketAddress);
            HttpURLConnection con = (HttpURLConnection) url.openConnection(webProxy);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setConnectTimeout(3000);
            System.out.println("Test");
            con.connect();

            int responseCode = con.getResponseCode();
            System.out.println("ProxyCheckerService, "+ host+ ":" + port + " " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("******, "+ host+ ":" + port + " OK" );
                isOnline = true;
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOnline;
    }

}
