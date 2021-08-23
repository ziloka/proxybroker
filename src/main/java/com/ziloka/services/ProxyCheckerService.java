package com.ziloka.services;

import java.io.IOException;
import java.net.*;

public class ProxyCheckerService {

    public ProxyCheckerService() {

    }

    // str syntax host:port
    public boolean check(String ipAddress) throws IOException {

        // https://www.baeldung.com/java-connect-via-proxy-server

        boolean isOnline = false;
        String host = ipAddress.substring(0, ipAddress.indexOf(":"));
        int port = Integer.parseInt(ipAddress.substring(ipAddress.indexOf(":") + 1));
        URL url = new URL("http://httpbin.org/ip?json");



        try {
            Proxy webProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            HttpURLConnection con = (HttpURLConnection) url.openConnection(webProxy);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            System.out.println("Host: " + host);
            System.out.println("Port: " + String.valueOf(port));
            int responseCode = con.getResponseCode();
            System.out.println("After Response Code");
            System.out.println("ProxyCheckerService, "+ host+ ":" + port + " " +responseCode);
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
