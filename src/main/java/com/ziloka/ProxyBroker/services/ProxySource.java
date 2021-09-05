package com.ziloka.ProxyBroker.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProxySource {

    private static final Logger logger = LogManager.getLogger(ProxyCollector.class);

    public String url;
    public ProxyType type;

    public ProxySource (ProxyType type) {
        this.type = type;
    }

    /**
     * @return String - Override Object#toString method
     */
    @Override
    public String toString(){
        return String.format("%s[url=%s,type=%s]", getClass().getSimpleName(), this.url, this.type);
    }
}