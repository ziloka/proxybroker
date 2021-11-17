package com.ziloka.ProxyBroker.services.models;

/**
 * Proxy Lookup Result
 */
public class LookupResult {

    final public String proxyHost;
    final public Integer proxyPort;
    private ProxyType proxyType;
    final private String isoCode;
    final private String countryName;

    /**
     * @param countryName - Country Name
     * @param isoCode - City Name
     */
    public LookupResult(String proxyHost, Integer proxyPort, String isoCode, String countryName) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.isoCode = !isoCode.equals(null) ? isoCode : "Unknown";
        this.countryName = !countryName.equals(null) ? countryName : "Unknown";
    }

    public void setProxyType(ProxyType proxyType) {
        this.proxyType = proxyType;
    }

    public ProxyType getProxyType() {
        return proxyType;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getCountryName() {
        return countryName;
    }

}
