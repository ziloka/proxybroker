package com.ziloka.ProxyBroker.subcmds.converters;

import com.ziloka.ProxyBroker.services.models.ProxyType;
import picocli.CommandLine.ITypeConverter;

import java.util.ArrayList;
import java.util.List;

// https://picocli.info/#_custom_type_converters
public class IProxyTypeConverter implements ITypeConverter<List<ProxyType>> {
    @Override
    public List<ProxyType> convert(String value) throws Exception {
        String[] values = value.split(",");
        List<ProxyType> result = new ArrayList<>();
        for(String proxyType: values){
            try {
                result.add(ProxyType.valueOf(proxyType));
            } catch (Exception e){
                System.out.println("Invalid value for option '--types' but was "+ proxyType);
                // https://man7.org/linux/man-pages/man3/errno.3.html
                System.exit(3);
            }
        }
        return result;
    }
}
