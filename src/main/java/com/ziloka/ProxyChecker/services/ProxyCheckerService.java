package com.ziloka.ProxyChecker.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class ProxyCheckerService {

    Logger logger = LogManager.getLogger(ProxyCollectorService.class);

    String ipAddress;
    String proxyType;

    public ProxyCheckerService(String ipAddress, String proxyType) {
        this.ipAddress = ipAddress;
        this.proxyType = proxyType;
    }

    // str syntax host:port
    public boolean check() throws IOException {

        // https://www.baeldung.com/java-connect-via-proxy-server
        // https://crunchify.com/how-to-run-multiple-threads-concurrently-in-java-executorservice-approach/

        boolean isOnline = false;
        String host = this.ipAddress.substring(0, this.ipAddress.indexOf(":"));
        int port = Integer.parseInt(this.ipAddress.substring(this.ipAddress.indexOf(":") + 1));
        URL url = new URL("http://httpbin.org/ip?json");

        Proxy.Type javaNetProxy = this.proxyType.matches("http(s?)") ? Proxy.Type.HTTP : Proxy.Type.SOCKS;

        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
            Proxy webProxy = new Proxy(javaNetProxy, inetSocketAddress);
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) url.openConnection(webProxy);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setReadTimeout(5000);
            con.setConnectTimeout(5000);
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
