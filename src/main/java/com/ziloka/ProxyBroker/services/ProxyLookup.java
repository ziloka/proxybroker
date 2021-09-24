package com.ziloka.ProxyBroker.services;

import com.google.gson.Gson;
import com.ziloka.ProxyBroker.services.models.LookupResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ProxyLookup {

    private static final Logger logger = LogManager.getLogger(ProxyCollector.class);

    String host;
    PoolingHttpClientConnectionManager poolConnManager;
    Gson gson;

    /**
     * @param host - Proxy host
     */
    public ProxyLookup(PoolingHttpClientConnectionManager poolConnManager, Gson gson, String host) {
        this.poolConnManager = poolConnManager;
        this.gson = gson;
        this.host = host;
    }

    /**
     * @return LookupResult - Proxy look up result
     * @throws IOException Failed I/O Operation
     */
    public LookupResult getInfo() throws IOException {
        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(this.poolConnManager)
                .build();
        HttpResponse response = client.execute(new HttpGet(String.format("http://ip-api.com/json/%s?fields=66846719", this.host)));
        InputStream inputStream = response.getEntity().getContent();
        // https://stackoverflow.com/a/35446009
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for(int length; (length = inputStream.read(buffer)) != -1;) {
            byteArrayStream.write(buffer, 0, length);
        }
        String content =  byteArrayStream.toString(StandardCharsets.UTF_8);
        LookupResult lookupResult = gson.fromJson(content, LookupResult.class);
        EntityUtils.consume(response.getEntity());
        return lookupResult;
    }

}
