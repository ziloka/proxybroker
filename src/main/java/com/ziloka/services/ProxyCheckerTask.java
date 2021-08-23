package com.ziloka.services;

import java.util.concurrent.TimeUnit;

public class ProxyCheckerTask implements Runnable {


    String types;
    String countries;
    String lvl;
    int limit;
    int totalProxyServerCount;

    public ProxyCheckerTask(
            String types
            , String countries
            , String lvl
            , int limit

    ) {
        this.types = types;
        this.countries = countries;
        this.lvl = lvl;
        this.limit = limit;
    }

    public void run() {


        ProxyCheckerService proxyCheckerService = new ProxyCheckerService();
        proxyCheckerService.check()
        try {
            Long duration = (long) (Math.random() * 10);
            System.out.println("Executing : " + name);
            TimeUnit.SECONDS.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void

}
