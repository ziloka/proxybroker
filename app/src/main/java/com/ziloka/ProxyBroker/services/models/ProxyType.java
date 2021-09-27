package com.ziloka.ProxyBroker.services.models;

/**
 * Valid Proxy Types
 */
// https://stackoverflow.com/a/40169031
public enum ProxyType {
    http("http"),
    https("https"),
    socks4("socks4"),
    socks5("socks5"),
    ALL("ALL");

    private final String proxyType;
    ProxyType(String proxyType){
        this.proxyType = proxyType;
    }

    @Override
    public String toString(){
        return this.proxyType;
    }
}