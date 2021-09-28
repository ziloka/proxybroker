package com.ziloka.ProxyBroker.services;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import com.ziloka.ProxyBroker.services.models.LookupResult;
import com.ziloka.ProxyBroker.services.models.ProxyType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;

public class ProxyLookup {

    private final Logger LOG = LogManager.getLogger(ProxyCollector.class);

    String host;
    Integer port;
    DatabaseReader dbReader;

    /**
     * @param dbReader - Max Mind Geo Ip2 database reader
     * @param host - Proxy host
     */
    public ProxyLookup(DatabaseReader dbReader, String host, Integer port) {
        this.dbReader = dbReader;
        this.host = host;
        this.port = port;
    }

    /**
     * @return LookupResult - Proxy look up result
     * @throws IOException Failed I/O Operation
     * @throws GeoIp2Exception Generic GeoIP2 Error
     */
    public LookupResult getInfo() throws IOException, GeoIp2Exception {
        InetAddress ipAddress = InetAddress.getByName(this.host);
        CountryResponse response = this.dbReader.country(ipAddress);
        Country country = response.getCountry();
        return new LookupResult(
                ipAddress.getHostAddress(),
                this.port,
                ProxyType.https,
                country.getIsoCode(),
                country.getName()
        );
    }

}
