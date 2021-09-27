package com.ziloka.ProxyBroker.services.models;

import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Country;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Proxy Lookup Result
 */
public class LookupResult {

    private final Logger logger = LogManager.getLogger(LookupResult.class);

    public ProxyType proxyType;
    private Country cityResponse;
    private String isoCode;
    private String countryName;

    /**
     * @param cityResponse - Maxmind GeoIp2 Model City Response Object
     * @param countryName - Country Name
     * @param isoCode - City Name
     */
    public LookupResult(Country cityResponse, String isoCode, String countryName) {
        this.cityResponse = cityResponse;
        this.isoCode = isoCode;
        this.countryName = countryName;
    }

    public Country getCityResponse() {
        return cityResponse;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getCountryName() {
        return countryName;
    }

}
