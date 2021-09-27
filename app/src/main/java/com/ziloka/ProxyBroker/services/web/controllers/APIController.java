package com.ziloka.ProxyBroker.services.web.controllers;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class does not do anything yet
 */
@RequestMapping("/api")
@RestController
@EnableAutoConfiguration
public class APIController {

    // Example curl http://localhost:8080/api?type=test
    @RequestMapping("/")
    public String api(@RequestParam(name = "type", required = false, defaultValue = "") String type,
                      @RequestParam(name = "countries", required = false, defaultValue = "") String countries,
                      @RequestParam(name = "lvl", required = false, defaultValue = "High") String lvl,
                      @RequestParam(name = "limit", required = false, defaultValue = "20") String limit) {
        return "Type: "+ type;
    }

}
