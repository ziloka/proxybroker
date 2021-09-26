package com.ziloka.ProxyBroker.cmds;

import com.ziloka.ProxyBroker.services.web.SpringBootConsoleApplication;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * Command under proxybroker command
 */
// https://picocli.info/#_executing_subcommands

@RestController
@EnableAutoConfiguration
@Command(name = "serve")
//@SpringBootApplication
public class ServeCommand implements Runnable {

    private static final Logger LOG = LogManager.getLogger(SpringBootConsoleApplication.class);

    @Option(names = { "--host", "-h" }, defaultValue = "127.0.0.1", type = String.class)
    private String host;

    @Option(names = { "--port", "-p" }, defaultValue = "8080", type = Integer.class)
    private Integer port;

    @Option(names = {"--verbose", "-v"}, defaultValue = "false", type = Boolean.class)
    private boolean isVerbose;

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