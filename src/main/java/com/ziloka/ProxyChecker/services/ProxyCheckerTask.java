package com.ziloka.ProxyChecker.services;

import com.ziloka.ProxyChecker.services.ProxyCheckerTask;

import java.io.IOException;
import java.util.HashMap;

public class ProxyCheckerTask implements Runnable {

    String proxy;
    String proxyType;
    HashMap<String, Boolean> onlineProxies;

    public ProxyCheckerTask(String proxy, String proxyType, HashMap<String, Boolean> onlineProxies) {
        this.proxy = proxy;
        this.proxyType = proxyType;
        this.onlineProxies = onlineProxies;
    }

    public void run() {

        try {
            ProxyCheckerService proxyCheckerService = new ProxyCheckerService(proxy, proxyType);
            boolean isOnline = proxyCheckerService.check();
            if(isOnline){
//                synchronized (this.onlineProxies){
                    this.onlineProxies.put(this.proxy, true);
//                }
            }

        } catch (IOException e) {
            // Don't print anything
            e.printStackTrace();
        }

    }


}