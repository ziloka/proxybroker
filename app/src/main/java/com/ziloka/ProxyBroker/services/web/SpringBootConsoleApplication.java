package com.ziloka.ProxyBroker.services.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.*;

// https://www.baeldung.com/java-picocli-create-command-line-program
// https://github.com/eugenp/tutorials/blob/master/libraries-2/src/main/java/com/baeldung/picocli/git/Application.java
// https://stackoverflow.com/a/24271946
// https://stackoverflow.com/a/62350097
// https://github.com/JanStureNielsen/manager-picocli

@RestController
@EnableAutoConfiguration
public class SpringBootConsoleApplication {

    private static Logger LOG = LoggerFactory.getLogger(SpringBootConsoleApplication.class);

    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootConsoleApplication.class, args);
    }

}