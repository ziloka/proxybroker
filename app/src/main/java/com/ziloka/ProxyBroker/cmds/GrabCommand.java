package com.ziloka.ProxyBroker.cmds;

import com.ziloka.ProxyBroker.services.models.ProxyType;
import com.ziloka.ProxyBroker.cmds.converters.IProxyTypeConverter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "grab")
public class GrabCommand implements Callable<Integer> {

    // https://picocli.info/apidocs/picocli/CommandLine.Option.html
    @Option(names = "--types", defaultValue = "http", type = IProxyTypeConverter.class, converter = IProxyTypeConverter.class)
    private List<ProxyType> types;

    @Option(names = "--countries", defaultValue = "")
    private String countries;

    @Option(names = "--lvl", defaultValue = "High")
    private String lvl;

    // https://picocli.info/#_handling_invalid_input
    @Option(names = {"--limit", "-l"}, defaultValue = "10", type = Integer.class)
    private int limit;

    @Option(names = {"--timeout", "-t"}, defaultValue = "8", type = Integer.class)
    private int timeout;

    @Option(names = {"--outfile", "-o"}, defaultValue = "proxies.txt")
    private String OutFile;

    @Option(names = {"--verbose", "-v"}, defaultValue = "false", type = Boolean.class)
    private boolean isVerbose;

    @Override
    public Integer call(){

//        try {
//            if(isVerbose) Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
//            else Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.OFF);
//
//            ConcurrentHashMap<String, LookupResult> onlineProxies = new ConcurrentHashMap<>();
//
//            LOG.debug("Collecting proxies");
//
//            ProxyCollector proxyProvider = new ProxyCollector(types, countries);
//            ArrayList<String> proxies = proxyProvider.getProxies(types);
//
//            BufferedWriter writer = new BufferedWriter(new FileWriter(OutFile));
//            writer.write(String.join("\n", proxies));
//            writer.close();

//            System.out.printf("Wrote %s proxies to %s\n", proxies.size(), OutFile);
//        } catch(IOException e){
//
//        }

        return 0;
    }

}
