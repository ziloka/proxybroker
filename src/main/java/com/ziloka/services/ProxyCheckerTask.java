package com.ziloka.services;

import java.io.IOException;
import java.util.concurrent.Callable;

public class ProxyCheckerTask implements Callable<Boolean> {

    String proxy;

    public ProxyCheckerTask(String proxy) {
        this.proxy = proxy;
    }

    public Boolean call() {

        boolean isOnline = false;
        try {
            ProxyCheckerService proxyCheckerService = new ProxyCheckerService();
            isOnline = proxyCheckerService.check(proxy);
        } catch (IOException e) {
            // Don't print anything
//            e.printStackTrace();
        }

        return isOnline;

    }


}