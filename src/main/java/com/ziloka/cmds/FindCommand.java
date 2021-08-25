package com.ziloka.cmds;

import com.ziloka.ProxyChecker;
import com.ziloka.services.Command;
import com.ziloka.services.ProxyCheckerTask;
import com.ziloka.services.ProxyCollectorService;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

public class FindCommand extends Command {

    String name = "find";

    Logger logger = ProxyChecker.logger;

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

    static HashMap<String, Boolean> onlineProxies = new HashMap<String, Boolean>();

    public void run(HashMap<String, ArrayList<String>> options) {

        logger.trace("Executing FindCommand#run Method");

        ProxyCollectorService proxyProvider = new ProxyCollectorService("http", "", "", 10);
        proxyProvider.setSources();
        ArrayList<String> proxies = proxyProvider.getProxies();

        // String#format
        // https://www.javatpoint.com/java-string-format
        logger.debug(String.format("There are %d unchecked proxies", proxies.size()));

        // https://stackoverflow.com/questions/12835077/java-multithread-multiple-requests-approach
        // Worker Threads
        // https://engineering.zalando.com/posts/2019/04/how-to-set-an-ideal-thread-pool-size.html
        // Set ideal thread pool size
        // 300ms on average to receive response from ProxyCheckerService#check
        // Simple interation on average takes more than 30+ minutes to check 200 proxies
        // On average takes ~10 minutes to check 200 proxies
        int NumOfThreads = Runtime.getRuntime().availableProcessors() * (1 + 300/50);
        ExecutorService executorService = Executors.newFixedThreadPool(NumOfThreads);
        logger.debug(String.format("Multithreading ProxyCheckTask.class using %d threads", NumOfThreads));
        for (String proxy : proxies) {
            try {
                // Implement Runnable instead of Callable
                ProxyCheckerTask proxyCheckerTask = new ProxyCheckerTask(proxy, this.onlineProxies);
                Future future = executorService.submit(proxyCheckerTask);
                // Calling the method get() blocks the current thread
                // and waits until the callable completes before returning the actual result
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        executorService.shutdown();
 while (!executorService.isTerminated()){

 }
        System.out.println("**size="+ this.onlineProxies.size());

        System.out.println("\nFinished all threads");
    }

}
