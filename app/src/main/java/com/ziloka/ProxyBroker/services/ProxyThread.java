package com.ziloka.ProxyBroker.services;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.ziloka.ProxyBroker.services.models.LookupResult;
import com.ziloka.ProxyBroker.services.models.ProxyType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyThread implements Runnable {

    private final Logger LOG = LogManager.getLogger(ProxyCollector.class);

    private final ConcurrentHashMap<String, LookupResult> onlineProxies;
    private String proxy;
    private String host;
    private int port;
    private List<ProxyType> types;
    private String lvl;
    private ProxyChecker proxyChecker;
    private ProxyLookup proxyLookup;

    /**
     * @param dbReader - MaxMind GeoIp2 Database reader
     * @param onlineProxies - Online Proxy Hashmap
     * @param proxy - Proxy syntax host:port
     * @param types - Proxy types
     * @param lvl - Proxy anonymity level
     */
    public ProxyThread(DatabaseReader dbReader, ConcurrentHashMap<String, LookupResult> onlineProxies, String externalIpAddr, String proxy, List<ProxyType> types, String lvl) {
        this.onlineProxies = onlineProxies;
        this.proxy = proxy;
        this.host = proxy.split(":")[0];
        this.port = Integer.parseInt(proxy.split(":")[1]);
        this.types = types;
        this.lvl = lvl;
        this.proxyChecker = new ProxyChecker(dbReader, onlineProxies, externalIpAddr, proxy, types);
        this.proxyLookup = new ProxyLookup(dbReader);
    }

    /**
     * Method executed when thread is executed
     */
    public void run(){
        try {
            boolean result = this.proxyChecker.check();
            boolean isLvl = this.lvl.length() == 0 || proxyChecker.lvl.equals(this.lvl);
            if(result && isLvl){
                LookupResult proxyInfo = this.proxyLookup.getInfo(host, port);
//                proxyInfo.setProxyType(this.proxyChecker.getProtocol());
                this.onlineProxies.put(this.proxy, proxyInfo);
            } else if(this.onlineProxies.get(this.proxy) != null) {
                this.onlineProxies.remove(this.proxy);
            }
        } catch (IOException | GeoIp2Exception e) {
            // Don't print anything
            e.printStackTrace();
        }
    }

}