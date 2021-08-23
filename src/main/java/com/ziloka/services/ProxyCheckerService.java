package com.ziloka.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class ProxyCheckerService {

    public ProxyCheckerService() {

    }

    // str syntax host:port
    public boolean check(String ipAddress) throws IOException {
        boolean isOnline = false;
        String host = ipAddress.substring(0, ipAddress.indexOf(":"));
        int port = Integer.parseInt(ipAddress.substring(ipAddress.indexOf(":") + 1));
        URL url = new URL("http://httpbin.org/ip?json");

        try {
            Proxy webProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            HttpURLConnection con = (HttpURLConnection) url.openConnection(webProxy);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = con.getResponseCode();
            System.out.println(responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                isOnline = true;
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOnline;
    }

}
