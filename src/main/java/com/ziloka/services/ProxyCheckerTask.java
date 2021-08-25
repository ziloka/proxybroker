package com.ziloka.services;

import com.ziloka.cmds.FindCommand;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;

public class ProxyCheckerTask implements Runnable {

    String proxy;
    HashMap<String, Boolean> onlineProxies;

    public static final Logger logger = LogManager.getLogger(FindCommand.class);

    public ProxyCheckerTask(String proxy, HashMap<String, Boolean> onlineProxies) {
        this.proxy = proxy;
        this.onlineProxies = onlineProxies;
    }

    public void run() {

        boolean isOnline = false;
        try {
            ProxyCheckerService proxyCheckerService = new ProxyCheckerService();
            String threadName = Thread.currentThread().getName();
            long startTime = System.nanoTime();
            isOnline = proxyCheckerService.check(proxy);
            long endTime = System.nanoTime();
            // There are 1,000,000 nano seconds in a millisecond
            long duration = (endTime - startTime)/10000000;
//            logger.debug(String.format("It took %dms to run ProxyCheckerService#check", duration));
            if(isOnline){
//                // Hashmaps automatically remove duplicates
//                // Unnecessary to synchronize object
                synchronized (this.onlineProxies){
                    onlineProxies.put(this.proxy, true);
                }
            } else {
                System.out.println(this.proxy);
            }

        } catch (IOException e) {
            // Don't print anything
            e.printStackTrace();
        }

    }


}