package com.ziloka.ProxyBroker.services;

import com.maxmind.geoip2.model.CityResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LookupResult {

    private static final Logger logger = LogManager.getLogger(LookupResult.class);

    public String countryName;
    public CityResponse cityResponse;
    public String cityName;
    public String postal;
    public String state;

    public LookupResult() { }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public void setCityResponse(CityResponse cityResponse) {
        this.cityResponse = cityResponse;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setPostal(String postal) {
        this.postal = postal;
    }

    public void setState(String state) {
        this.state = state;
    }
}
