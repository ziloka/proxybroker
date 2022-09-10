package com.ziloka.ProxyBroker.services.models;

/**
 * Proxy Lookup Result
 */
public class LookupResult {

    final private String proxyHost;
    final private Integer proxyPort;
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
        this.isoCode = isoCode != null ? isoCode : "Unknown";
        this.countryName = countryName != null ? countryName : "Unknown";
    }

    public void setProxyType(ProxyType proxyType) {
        this.proxyType = proxyType;
    }

    public String getProxyHost() {
      return proxyHost;
    }

    public Integer getProxyPort() {
      return proxyPort;
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
