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
            proxyCheckerService.check(proxy);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
