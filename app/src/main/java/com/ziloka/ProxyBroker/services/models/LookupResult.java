package com.ziloka.ProxyBroker.services.models;

import com.maxmind.geoip2.record.Country;

/**
 * Proxy Lookup Result
 */
public class LookupResult {

    final private String proxyHost;
    final private Integer proxyPort;
    final private ProxyType proxyType;
    final private String isoCode;
    final private String countryName;

    /**
     * @param countryName - Country Name
     * @param isoCode - City Name
     */
    public LookupResult(String proxyHost, Integer proxyPort, ProxyType proxyType, String isoCode, String countryName) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyType = proxyType;
        this.isoCode = isoCode;
        this.countryName = countryName;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getCountryName() {
        return countryName;
    }

}
