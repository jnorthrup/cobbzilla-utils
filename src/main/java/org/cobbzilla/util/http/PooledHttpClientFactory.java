package org.cobbzilla.util.http;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.cobbzilla.util.reflect.ObjectFactory;

import java.util.Map;

public class PooledHttpClientFactory implements ObjectFactory<CloseableHttpClient> {

    private String host;
    private int maxConnections;

    @java.beans.ConstructorProperties({"host", "maxConnections"})
    public PooledHttpClientFactory(String host, int maxConnections) {
        this.host = host;
        this.maxConnections = maxConnections;
    }

    @Override public CloseableHttpClient create() {
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxConnections);
        cm.setMaxPerRoute(new HttpRoute(new HttpHost(host)), maxConnections);
        return HttpClients.custom()
                .setConnectionManager(cm)
                .setConnectionManagerShared(true)
                .build();
    }

    @Override public CloseableHttpClient create(Map<String, Object> ctx) { return create(); }

    public String getHost() {
        return this.host;
    }

    public int getMaxConnections() {
        return this.maxConnections;
    }
}
