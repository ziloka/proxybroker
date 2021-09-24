package com.ziloka.ProxyBroker.services.models;

public class LookupResult {

    private String status;
    private String message;
    private String continent;
    private String continentCode;
    private String country;
    private String countryCode;
    private String region;
    private String regionName;
    private String city;
    private String district;
    private String zip;
    private double lat;
    private double lon;
    private String timezone;
    private int offset;
    private String currency;
    private String isp;
    private String org;
    private String as;
    private String asName;
    private String reverse;
    private boolean mobile;
    private boolean proxy;
    private boolean hosting;
    private String query;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getContinent() {
        return continent;
    }

    public String getContinentCode() {
        return continentCode;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getRegion() {
        return region;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getZip() {
        return zip;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getTimezone() {
        return timezone;
    }

    public int getOffset() {
        return offset;
    }

    public String getCurrency() {
        return currency;
    }

    public String getIsp() {
        return isp;
    }

    public String getOrg() {
        return org;
    }

    public String getAs() {
        return as;
    }

    public String getAsName() {
        return asName;
    }

    public String getReverse() {
        return reverse;
    }

    public boolean isMobile() {
        return mobile;
    }

    public boolean isProxy() {
        return proxy;
    }

    public boolean isHosting() {
        return hosting;
    }

    public String getQuery() {
        return query;
    }
}
