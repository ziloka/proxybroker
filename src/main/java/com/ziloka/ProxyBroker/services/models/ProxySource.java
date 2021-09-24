package com.ziloka.ProxyBroker.services.models;

import com.ziloka.ProxyBroker.services.ProxyCollector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Used to serialize json proxy object
 */
public class ProxySource {

    private static final Logger logger = LogManager.getLogger(ProxyCollector.class);

    public String url;
    public ProxyType type;

    /**
     * @return String - Override Object#toString method
     */
    @Override
    public String toString(){
        return String.format("%s[url=%s,type=%s]", getClass().getSimpleName(), this.url, this.type);
    }
}