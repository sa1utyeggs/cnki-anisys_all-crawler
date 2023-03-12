package com.hh.entity.application;

import org.apache.http.Header;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ab875
 */
public class Task {
    protected final Map<String, Object> storages;
    protected final Map<String, String> headers;

    public Task() {
        storages = new HashMap<>(16);
        headers = new HashMap<>(32);
    }


    public Object setStorage(String k, Object v) {
        return storages.put(k, v);
    }

    public Object removeStorage(String k) {
        return storages.remove(k);
    }

    public Object getStorage(String k) {
        return storages.get(k);
    }


    // Header

    public Object setHeader(String k, String v) {
        return headers.put(k, v);
    }

    public void setHeaders(Header[] headers) {
        for (Header header : headers) {
            this.headers.put(header.getName(), header.getValue());
        }
    }

    public Object removeHeader(String k) {
        return headers.remove(k);
    }

    public String getHeader(String k) {
        return headers.get(k);
    }
}
