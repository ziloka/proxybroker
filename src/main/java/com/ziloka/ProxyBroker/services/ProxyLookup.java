package com.ziloka.ProxyBroker.services;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;

public class ProxyLookup {

    private static final Logger logger = LogManager.getLogger(ProxyCollector.class);

    String host;
    DatabaseReader dbReader;

    public ProxyLookup(DatabaseReader dbReader, String host) {
        this.dbReader = dbReader;
        this.host = host;
    }

    public LookupResult getInfo(){
        LookupResult result = new LookupResult();
        try {
            logger.debug("Get InetAddress");
            InetAddress ipAddress = InetAddress.getByName(this.host);
            logger.debug("Getting City Response");
            CityResponse response = this.dbReader.city(ipAddress);
            result.setCityResponse(response);
            result.setCountryName(response.getCountry().getName());
            result.setCityName(response.getCity().getName());
            result.setPostal(response.getPostal().getCode());
            result.setState(response.getLeastSpecificSubdivision().getName());
        } catch (IOException | GeoIp2Exception e){
            e.printStackTrace();
        }
        return result;
    }

}
