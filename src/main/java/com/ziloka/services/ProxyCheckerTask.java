package com.ziloka.services;

import java.io.IOException;

public class ProxyCheckerTask implements Runnable {

    String proxy;

    public ProxyCheckerTask(String proxy) {
        this.proxy = proxy;
    }

    public void run() {

        try {
            ProxyCheckerService proxyCheckerService = new ProxyCheckerService();
            boolean isOnline = proxyCheckerService.check(proxy);
            if(isOnline){

            }
        } catch (IOException e) {
            // Don't print anything
//            e.printStackTrace();
        }

    }


}
