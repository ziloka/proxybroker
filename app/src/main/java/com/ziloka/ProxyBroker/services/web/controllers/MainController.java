package com.ziloka.ProxyBroker.services.web.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

}