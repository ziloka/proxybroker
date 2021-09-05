package com.ziloka.ProxyBroker.services;

import com.maxmind.geoip2.DatabaseReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProxyThread implements Runnable {

    private static final Logger logger = LogManager.getLogger(ProxyCollector.class);

    HashMap<String, LookupResult> onlineProxies;
    String proxy;
    String host;
    int port;
    String types;
    ProxyChecker proxyChecker;
    ProxyLookup proxyLookup;

    public ProxyThread(DatabaseReader dbReader, HashMap<String, LookupResult> onlineProxies, String proxy, String types) {
        this.onlineProxies = onlineProxies;
        this.proxy = proxy;
        this.host = proxy.split(":")[0];
        this.port = Integer.parseInt(proxy.split(":")[1]);
        this.types = types;
        this.proxyChecker = new ProxyChecker(dbReader, this.onlineProxies, this.proxy, types);
        this.proxyLookup = new ProxyLookup(dbReader, this.host);
    }

    public void run(){
        try {
            if(this.proxyChecker.check()){
                synchronized (this.onlineProxies){
                    this.onlineProxies.put(this.proxy, this.proxyLookup.getInfo());
                }
            }
        } catch (IOException e) {
            // Don't print anything
            e.printStackTrace();
        }
    }

}
