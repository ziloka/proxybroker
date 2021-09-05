package com.ziloka.ProxyBroker.services;

import com.maxmind.geoip2.model.CityResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LookupResult {

    private static final Logger logger = LogManager.getLogger(LookupResult.class);

    public CityResponse cityResponse;
    public String countryName;
    public String cityName;
    public String postal;
    public String state;

    /**
     * @param cityResponse - Maxmind GeoIp2 Model City Response Object
     * @param countryName - Country Name
     * @param cityName - City Name
     * @param postal - Postal Code
     * @param state - State Name
     */
    public LookupResult(CityResponse cityResponse, String countryName, String cityName, String postal, String state) {
        this.countryName = countryName;
        this.cityResponse = cityResponse;
        this.cityName = cityName;
        this.postal = postal;
        this.state = state;
    }

}
