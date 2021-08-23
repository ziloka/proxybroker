package com.ziloka.cmds;

import com.ziloka.services.Command;
import com.ziloka.services.ProxyCheckerService;
import com.ziloka.services.ProxyCheckerTask;
import com.ziloka.services.ProxyCollectorService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FindCommand extends Command {

    String name = "find";

    public FindCommand(){
        super();
    }

    @Override
    public String getCommand() {
        return name;
    }

    @Override
    public String getDescr(){
        return "Find and Check proxies";
    }

    @Override
    public String getOptions(){
        return "--types HTTP";
    }

    static ArrayList<String> proxies;

    public void run(String[] args) {

//        ProxyCollectorService proxyProvider = new ProxyCollectorService(types, countries, lvl, limit);
        ProxyCollectorService proxyProvider = new ProxyCollectorService("http", "", "", 10);
        proxyProvider.setSources();
        proxies = proxyProvider.getProxies();

            // https://stackoverflow.com/questions/12835077/java-multithread-multiple-requests-approach
            // Worker Threads
            // More threads
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            for (String proxy : proxies) {
                Runnable worker = new ProxyCheckerTask(proxy);
                executor.execute(worker);
            }
            executor.shutdown();
            while(!executor.isTerminated()){

            }
            System.out.println("\nFinished all threads");

    }

}
