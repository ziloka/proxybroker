package com.ziloka.cmds;

import com.ziloka.ProxyChecker;
import com.ziloka.services.Command;
import com.ziloka.services.ProxyCheckerTask;
import com.ziloka.services.ProxyCollectorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

public class FindCommand extends Command {

    class FindCommandOptions {

        public final String[] type = new String[]{"http", "https", "socks4", "socks5"};
        public final String countries = "";
        public final String lvl = "High";
        public final int limit = 10;

    }

    String name = "find";

    @Override
    public String getCommand() {
        return name;
    }

    @Override
    public String getDescr(){
        return "Find and Check proxies";
    }

    Logger logger = LogManager.getLogger(FindCommand.class);
    static HashMap<String, Boolean> onlineProxies = new HashMap<String, Boolean>();

    public void run(HashMap<String, ArrayList<String>> options) {

        logger.debug("Collecting proxies");

        // Parse options
        String proxyType = options.get("types") != null ? options.get("types").get(0).toLowerCase() : "";
        String countries = options.get("countries") != null ? options.get("countries").get(0).toLowerCase() : "";
        String lvl = options.get("lvl") != null ? options.get("lvl").get(0).toLowerCase() : "high";
        int limit = options.get("limit") != null ? Integer.parseInt(options.get("limit").get(0)) : 10;

        ProxyCollectorService proxyProvider = new ProxyCollectorService(proxyType, countries, lvl);
        proxyProvider.setSources();
        ArrayList<String> proxies = proxyProvider.getProxies(proxyType);
        // String#format
        // https://www.javatpoint.com/java-string-format
        logger.debug(String.format("There are %d unchecked proxies", proxies.size()));

        // Simple iteration on average takes more than 30+ minutes to check 200 proxies
        // On average takes ~20 seconds to check 200 proxies
        ExecutorService executorService = Executors.newCachedThreadPool();
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;
        for (String proxy : proxies) {
            try {
                ProxyCheckerTask proxyCheckerTask = new ProxyCheckerTask(proxy, proxyType, this.onlineProxies);
                executorService.submit(proxyCheckerTask);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        logger.debug(String.format("Multithreading ProxyCheckTask.class using %d threads", threadPoolExecutor.getActiveCount()));

        executorService.shutdown();
        // Wait for all threads states to be terminated
        while (!executorService.isTerminated()){

        }
        logger.debug(String.format("There are %d online proxies", this.onlineProxies.size()));

        System.out.println("\nFinished all threads");
    }

}
