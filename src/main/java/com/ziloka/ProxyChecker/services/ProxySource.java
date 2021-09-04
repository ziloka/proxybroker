package com.ziloka.ProxyChecker.services;

public class ProxySource {
    public String url;
    public ProxyType type;

    public ProxySource (ProxyType type) {
      this.type = type;
    }

    @Override
    public String toString(){
        return String.format("%s[url=%s,type=%s]", getClass().getSimpleName(), this.url, this.type);
    }
}