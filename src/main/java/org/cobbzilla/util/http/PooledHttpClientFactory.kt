package org.cobbzilla.util.http

import org.apache.http.HttpHost
import org.apache.http.conn.routing.HttpRoute
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.cobbzilla.util.reflect.ObjectFactory

class PooledHttpClientFactory @java.beans.ConstructorProperties("host", "maxConnections")
constructor(val host: String, val maxConnections: Int) : ObjectFactory<CloseableHttpClient> {

    override fun create(): CloseableHttpClient {
        val cm = PoolingHttpClientConnectionManager()
        cm.maxTotal = maxConnections
        cm.setMaxPerRoute(HttpRoute(HttpHost(host)), maxConnections)
        return HttpClients.custom()
                .setConnectionManager(cm)
                .setConnectionManagerShared(true)
                .build()
    }

    override fun create(ctx: Map<String, Any>): CloseableHttpClient {
        return create()
    }
}
