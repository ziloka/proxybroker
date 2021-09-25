package com.ziloka.ProxyBroker.cmds;

import com.ziloka.ProxyBroker.services.web.Application;

import org.springframework.boot.SpringApplication;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Command under proxybroker command
 */
// https://picocli.info/#_executing_subcommands
@SuppressWarnings("ALL")
@Command(name = "serve")
public class ServeCommand implements Runnable {

    private static final Logger logger = LogManager.getLogger(ServeCommand.class);

    @Option(names = { "--host", "-h" }, defaultValue = "127.0.0.1", type = String.class)
    private String host;

    @Option(names = { "--port", "-p" }, defaultValue = "8080", type = Integer.class)
    private Integer port;

    @Option(names = {"--verbose", "-v"}, defaultValue = "false", type = Boolean.class)
    private boolean isVerbose;

    /**
     * Set commandline options
     * @param args System arguments
     */
    public static void main(String[] args) {
        CommandLine cli = new CommandLine(new FindCommand());
        cli.setOptionsCaseInsensitive(true);
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }

    /**
     * Executes when user runs "proxybroker serve"
     */
    @Override
    public void run() {
        // https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot-cli/src/main/java/org/springframework/boot/cli/SpringCli.java

        SpringApplication.run(Application.class);
    }

}