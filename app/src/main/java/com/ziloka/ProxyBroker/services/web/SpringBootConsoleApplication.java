package com.ziloka.ProxyBroker.services.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// http://www.masterspringboot.com/web/rest-services/rest-controller-not-found-in-spring-boot-applications-how-to-solve-it/
// https://stackoverflow.com/a/24271946

@RestController
@EnableAutoConfiguration
public class SpringBootConsoleApplication {

    private static Logger LOG = LoggerFactory.getLogger(SpringBootConsoleApplication.class);

    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }

}