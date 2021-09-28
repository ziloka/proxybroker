package com.ziloka.ProxyBroker.services.models;

public class ProxySource {

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