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

    /**
     * @param dbReader - Max Mind geoip2 database reader
     * @param host - Proxy host
     */
    public ProxyLookup(DatabaseReader dbReader, String host) {
        this.dbReader = dbReader;
        this.host = host;
    }

    /**
     * @return LookupResult - Proxy look up result
     * @throws IOException
     * @throws GeoIp2Exception
     */
    public LookupResult getInfo() throws IOException, GeoIp2Exception {
        InetAddress ipAddress = InetAddress.getByName(this.host);
        CityResponse response = this.dbReader.city(ipAddress);
        return new LookupResult(
                response,
                response.getCountry().getName(),
                response.getCity().getName(),
                response.getPostal().getCode(),
                response.getLeastSpecificSubdivision().getName()
        );
    }

}
