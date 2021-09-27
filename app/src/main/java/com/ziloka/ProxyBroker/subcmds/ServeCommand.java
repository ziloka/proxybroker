package com.ziloka.ProxyBroker.subcmds;

import com.ziloka.ProxyBroker.services.web.SpringBootConsoleApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Command under proxybroker command
 */

// https://www.baeldung.com/java-picocli-create-command-line-program
// https://github.com/eugenp/tutorials/blob/master/libraries-2/src/main/java/com/baeldung/picocli/git/Application.java

@Command(name = "serve")
@Component
public class ServeCommand implements Runnable {

    private final Logger LOG = LogManager.getLogger(SpringBootConsoleApplication.class);

    @Option(names = { "--host", "-h" }, defaultValue = "127.0.0.1", type = String.class)
    private String host;

    @Option(names = { "--port", "-p" }, defaultValue = "8080", type = Integer.class)
    private Integer port;

    @Option(names = {"--verbose", "-v"}, defaultValue = "false", type = Boolean.class)
    private boolean isVerbose;

    private int exitCode;

    /**
     * Here for debugging purposes
     * @param args - System Arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBootConsoleApplication.class);
    }

    /**
     * Method that gets executed by picocli
     * Required by java.lang.Runnable interface
     */
    @Override
    public void run() {
        SpringApplication.run(SpringBootConsoleApplication.class);
    }

}