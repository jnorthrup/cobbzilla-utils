package org.cobbzilla.util.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.experimental.Accessors;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.cobbzilla.util.collection.NameAndValue;
import org.cobbzilla.util.json.JsonUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.apache.http.HttpHeaders.CONTENT_LENGTH;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.string.StringUtil.UTF8cs;

@Accessors(chain=true)
public class HttpResponseBean {

    public static final HttpResponseBean OK = new HttpResponseBean().setStatus(HttpStatusCodes.OK);
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HttpResponseBean.class);

    private int status;
    private List<NameAndValue> headers;
    @JsonIgnore
    private byte[] entity;
    private long contentLength;
    private String contentType;

    @JsonIgnore public boolean isOk() { return (status / 100) == 2; }

    public Map<String, Object> toMap () {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", status);
        if (!empty(headers)) map.put("headers", headers.toArray());
        map.put("entity", hasContentType() ? HttpContentTypes.escape(contentType(), getEntityString()) : getEntityString());
        return map;
    }

    public boolean hasHeader (String name) { return !empty(getHeaderValues(name)); }
    public boolean hasContentType () { return contentType != null || hasHeader(HttpHeaders.CONTENT_TYPE); }
    public String contentType () { return contentType != null ? contentType : getFirstHeaderValue(HttpHeaders.CONTENT_TYPE); }

    public void addHeader(String name, String value) {
        if (headers == null) headers = new ArrayList<>();
        if (name.equalsIgnoreCase(CONTENT_TYPE)) setContentType(value);
        else if (name.equalsIgnoreCase(CONTENT_LENGTH)) setContentLength(Long.valueOf(value));
        headers.add(new NameAndValue(name, value));
    }

    public HttpResponseBean setEntityBytes(byte[] bytes) { this.entity = bytes; return this; }

    public HttpResponseBean setEntity (InputStream entity) {
        try {
            this.entity = entity == null ? null : IOUtils.toByteArray(entity);
            return this;
        } catch (IOException e) {
            return die("setEntity: error reading stream: " + e, e);
        }
    }

    public boolean hasEntity () { return !empty(entity); }

    public String getEntityString () {
        try {
            return entity == null ? null : new String(entity, UTF8cs);
        } catch (Exception e) {
            log.warn("getEntityString: error parsing bytes: "+e);
            return null;
        }
    }

    public <T> T getEntity (Class<T> clazz) {
        return entity == null ? null : JsonUtil.fromJsonOrDie(getEntityString(), clazz);
    }

    public Collection<String> getHeaderValues (String name) {
        final List<String> values = new ArrayList<>();
        if (!empty(headers)) for (NameAndValue header : headers) if (header.getName().equalsIgnoreCase(name)) values.add(header.getValue());
        return values;
    }


    public String getFirstHeaderValue (String name) {
        if (empty(headers)) return null;
        for (NameAndValue header : headers) if (header.getName().equalsIgnoreCase(name)) return header.getValue();
        return null;
    }

    public HttpResponseBean setHttpHeaders(Header[] headers) {
        for (Header header : headers) {
            addHeader(header.getName(), header.getValue());;
        }
        return this;
    }

    public HttpResponseBean setHttpHeaders(Map<String, List<String>> h) {
        if (empty(h)) return this;
        for (Map.Entry<String, List<String>> e : h.entrySet()) {
            if (!empty(e.getKey())) {
                for (String v : e.getValue()) {
                    if (!empty(v)) addHeader(e.getKey(), v);
                }
            }
        }
        return this;
    }

    public java.lang.String toString() {
        return "HttpResponseBean(status=" + this.status + ", headers=" + this.headers + ")";
    }

    public int getStatus() {
        return this.status;
    }

    public List<NameAndValue> getHeaders() {
        return this.headers;
    }

    public byte[] getEntity() {
        return this.entity;
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public String getContentType() {
        return this.contentType;
    }

    public HttpResponseBean setStatus(int status) {
        this.status = status;
        return this;
    }

    public HttpResponseBean setHeaders(List<NameAndValue> headers) {
        this.headers = headers;
        return this;
    }

    public HttpResponseBean setContentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public HttpResponseBean setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
}
