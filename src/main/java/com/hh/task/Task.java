package com.hh.task;

import com.hh.function.base.ContextSingletonFactory;
import com.hh.function.http.HttpConnectionPool;
import org.apache.http.Header;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author ab875
 */
public abstract class Task<T> implements Callable<T> {
    protected final Map<String, Object> storages;
    protected final Map<String, String> headers;

    /**
     * spring
     */
    protected static final ApplicationContext CONTEXT = ContextSingletonFactory.getInstance();
    protected static final HttpConnectionPool HTTP_CONNECTION_POOL = CONTEXT.getBean("httpConnectionPool", HttpConnectionPool.class);


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
