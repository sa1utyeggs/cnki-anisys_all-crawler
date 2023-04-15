package com.hh.entity.system;

import lombok.Data;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.concurrent.Callable;

/**
 * @author 86183
 */
@Data
public class HttpTask implements Callable<CloseableHttpResponse> {
    private final HttpRequestBase request;
    private final CloseableHttpClient executor;
    private Thread currentThread;
    /**
     * disposer 是 abort() 方法；
     * 用于 舍弃链接
     */
    private final RequestDisposer disposer;

    public HttpTask(HttpRequestBase request, CloseableHttpClient executor) {
        this.request = request;
        this.executor = executor;
        this.disposer = request::abort;
    }

    @Override
    public CloseableHttpResponse call() {
        try {
            currentThread = Thread.currentThread();
            // 线程因为 readSocket0() 方法，有可能卡死在这个位置没有反应
            return executor.execute(request, HttpClientContext.create());
        } catch (Exception e) {
            e.printStackTrace();
            // 以防万一，关闭连接
            disposer.dispose();
        }
        return null;
    }

    public void abortRequest() {
        // request::abort
        disposer.dispose();
    }

    @Override
    public String toString() {
        return "HttpTask{" +
                "request=" + request +
                ", executor=" + executor +
                ", currentThread=" + currentThread +
                ", disposer=" + disposer +
                '}';
    }

    @FunctionalInterface
    interface RequestDisposer {
        /**
         * 处理函数
         */
        void dispose();
    }
}
