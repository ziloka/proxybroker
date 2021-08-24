package com.ziloka.cmds;

import com.ziloka.services.Command;
import com.ziloka.services.ProxyCheckerService;
import com.ziloka.services.ProxyCheckerTask;
import com.ziloka.services.ProxyCollectorService;

import java.io.IOException;
import java.net.CacheRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

public class FindCommand extends Command {

    String name = "find";

//    public FindCommand(){}

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

    public void run(HashMap<String, ArrayList<String>> options) {

        ProxyCollectorService proxyProvider = new ProxyCollectorService("http", "", "", 10);
        proxyProvider.setSources();
        proxies = proxyProvider.getProxies();

        // https://stackoverflow.com/questions/12835077/java-multithread-multiple-requests-approach
        // Worker Threads
        // https://engineering.zalando.com/posts/2019/04/how-to-set-an-ideal-thread-pool-size.html
        // Set ideal thread pool size
        int NumOfThreads = Runtime.getRuntime().availableProcessors() * (1 + 50/5);
        ExecutorService service = Executors.newFixedThreadPool(NumOfThreads);
        for (String proxy : proxies) {
            try {
                Future<Boolean> proxyCheckerTask = service.submit(new ProxyCheckerTask(proxy));
                Boolean isWorking = proxyCheckerTask.get();
                System.out.println("Proxy: "+proxy + " isWorking: "+ isWorking);
            } catch (InterruptedException | ExecutionException e){
                e.printStackTrace();
            }
        }

    }

}
