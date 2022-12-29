package com.hh.utils;

import cn.hutool.core.util.URLUtil;
import com.hh.function.ipproxy.IpProxy;
import com.hh.function.ipproxy.ProxyIp;
import com.hh.function.ipproxy.XiaoxiangIpProxy;
import com.hh.function.system.ContextSingltonFactory;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import javafx.beans.binding.ObjectExpression;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.context.ApplicationContext;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author 86183
 */
public class HttpConnectionPoolUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpConnectionPoolUtil.class);

    /**
     * 指客户端和服务器建立连接的 timeout 10s
     */
    private static final int CONNECT_TIMEOUT = 2000;
    /**
     * 指客户端和服务器建立连接后，客户端从服务器读取数据的 timeout
     */
    private static final int SOCKET_TIMEOUT = 2 * 60 * 1000;
    /**
     * 指 MAX_TOTAL：一次最多接受的请求数量
     */
    private static final int MAX_CONN = 2;
    /**
     * 某一个服务每次能并行接收的请求数量
     */
    private static final int MAX_PER_ROUTE = 1;
    private static final int MAX_ROUTE = 1;
    /**
     * 空闲线程的存活时间
     */
    private static final int IDLE_TIMEOUT = 100_000;
    /**
     * 空闲线程检查时间
     */
    private static final int MONITOR_INTERVAL = 5000;

    /**
     * 发送请求的客户端单例
     */
    private static volatile CloseableHttpClient httpClient;
    /**
     * 连接池管理类
     */
    private static PoolingHttpClientConnectionManager manager;
    private static ScheduledExecutorService monitorExecutor;

    /**
     * 线程锁，用于线程安全
     */
    private final static Object SYNC_LOCK = new Object();

    /**
     * 请求 header 与 cookie
     */
    public final static Map<String, String> BASE_HEADERS = new HashMap<>(16);
    private static String cookie;

    /**
     * Spring 容器 Bean
     */
    private static final ApplicationContext CONTEXT = ContextSingltonFactory.getInstance();
    private static final IpProxy IP_PROXY = CONTEXT.getBean("xiaoxiangIpProxy", XiaoxiangIpProxy.class);


    static {
        try {
            // 初始化 cookie
            URL resource = JsonUtils.class.getResource("/");
            if (resource != null) {
                String baseUrl = resource.getPath();
                File cookieFile = new File(baseUrl + "cookie.txt");
                // 从文件里读 cookie
                cookie = FileUtils.readFileToString(cookieFile, StandardCharsets.UTF_8);
                // 注意替换 ; / ? : @ = &
                cookie = URLUtil.normalize(cookie);
            }

            // 由 Spring 初始化 代理 IP 池
        } catch (Exception e) {
            System.out.println("ConnectionFactory：初始化失败");
        }
        // 初始化基础 header
        BASE_HEADERS.put("Accept", "text/html, */*; q=0.01");
        BASE_HEADERS.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        BASE_HEADERS.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36");
        BASE_HEADERS.put("Host", "kns.cnki.net");
        BASE_HEADERS.put("Origin", "https://kns.cnki.net");
        BASE_HEADERS.put("Cookie", cookie);
        BASE_HEADERS.put("Connection", "keep-alive");
    }


    /**
     * 对http请求进行基本设置
     *
     * @param httpRequestBase http请求
     */
    private static void setRequestConfig(HttpRequestBase httpRequestBase) {
        ProxyIp ip = IP_PROXY.getIp();
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECT_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                // 设置代理
                .setProxy(new HttpHost(ip.getIp(), ip.getPort()))
                .build();
        httpRequestBase.setConfig(requestConfig);
    }

    @SuppressWarnings({"all"})
    public static CloseableHttpClient getHttpClient(String url) {
        String[] split = url.split("/");
        String hostName = split[2];
        int port = 80;
        if ("https:".equals(split[0])){
            port = 443;
        }
        // 如果 url 中指定了端口，则使用指定端口 并 去掉 hostName 中的 “:”
        if (hostName.contains(":")) {
            String[] args = hostName.split(":");
            hostName = args[0];
            port = Integer.parseInt(args[1]);
        }

        if (httpClient == null) {
            // 双重锁定检查
            synchronized (SYNC_LOCK) {
                if (httpClient == null) {
                    httpClient = createHttpClient(hostName, port);
                    // 开启监控线程,对异常和空闲线程进行关闭
                    monitorExecutor = Executors.newScheduledThreadPool(1);
                    monitorExecutor.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            // 关闭异常连接
                            manager.closeExpiredConnections();
                            // 关闭空闲连接
                            manager.closeIdleConnections(IDLE_TIMEOUT, TimeUnit.MILLISECONDS);
                            logger.trace("close expired and idle for over " + IDLE_TIMEOUT / 1000 + "s connection");
                        }
                    }, MONITOR_INTERVAL, MONITOR_INTERVAL, TimeUnit.MILLISECONDS);
                }
            }
        }
        return httpClient;
    }

    /**
     * 根据 host 和 port 构建httpclient实例
     *
     * @param host 要访问的域名
     * @param port 要访问的端口
     * @return httpclient
     */
    public static CloseableHttpClient createHttpClient(String host, int port) {
        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", plainSocketFactory)
                .register("https", sslSocketFactory).build();

        manager = new PoolingHttpClientConnectionManager(registry);

        // 最大连接数
        manager.setMaxTotal(MAX_CONN);
        // 路由最大连接数
        manager.setDefaultMaxPerRoute(MAX_PER_ROUTE);

        HttpHost httpHost = new HttpHost(host);
        manager.setMaxPerRoute(new HttpRoute(httpHost), MAX_ROUTE);

        // 请求失败时,进行请求重试
        HttpRequestRetryHandler handler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                if (i > 3) {
                    //重试超过3次,放弃请求
                    logger.error("retry has more than 3 time, give up request");
                    return false;
                }
                if (e instanceof NoHttpResponseException) {
                    //服务器没有响应,可能是服务器断开了连接,应该重试
                    logger.error("receive no response from server, retry");
                    return true;
                }
                if (e instanceof SSLHandshakeException) {
                    // SSL握手异常
                    logger.error("SSL hand shake exception");
                    return false;
                }
                if (e instanceof InterruptedIOException) {
                    //超时
                    logger.error("InterruptedIOException");
                    return false;
                }
                if (e instanceof UnknownHostException) {
                    // 服务器不可达
                    logger.error("server host unknown");
                    return false;
                }
                if (e instanceof ConnectTimeoutException) {
                    // 连接超时
                    logger.error("Connection Time out");
                    return false;
                }
                if (e instanceof SSLException) {
                    logger.error("SSLException");
                    return false;
                }

                HttpClientContext context = HttpClientContext.adapt(httpContext);
                HttpRequest request = context.getRequest();
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    // 如果请求不是关闭连接的请求
                    return true;
                }
                return false;
            }
        };

        return HttpClients.custom().setConnectionManager(manager).setRetryHandler(handler).build();
    }

    /**
     * 发送 get 请求
     *
     * @param url    URL
     * @param params 参数
     * @return Document
     */
    public static Document get(String url, Map<String, Object> params, Map<String, String> excessHeaders) {
        HttpGet httpGet = new HttpGet();
        // 基础 header
        setHeader(httpGet, BASE_HEADERS);
        // 额外 header
        setHeader(httpGet, excessHeaders);
        setRequestConfig(httpGet);
        // 设置参数
        setGetParams(httpGet, url, params);

        CloseableHttpResponse response = null;
        InputStream in = null;
        Document document = null;
        try {
            response = getHttpClient(url).execute(httpGet, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                in = entity.getContent();
                document = Jsoup.parse(IOUtils.toString(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (response != null) {
                    // 将 response 关闭后，就能将连接放回连接池
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return document;
    }

    /**
     * 发送 post 请求
     *
     * @param url    URL
     * @param params 参数
     * @return Document
     */
    public static Document post(String url, Map<String, Object> params, Map<String, String> excessHeaders) {
        HttpPost httpPost = new HttpPost(url);
        // 基础 header
        setHeader(httpPost, BASE_HEADERS);
        // 额外 header
        setHeader(httpPost, excessHeaders);
        setRequestConfig(httpPost);
        setPostParams(httpPost, params);
        CloseableHttpResponse response = null;
        InputStream in = null;
        Document document = null;
        try {
            response = getHttpClient(url).execute(httpPost, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                in = entity.getContent();
                document = Jsoup.parse(IOUtils.toString(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return document;
    }

    /**
     * 设置 post 请求的参数
     *
     * @param httpPost post
     * @param params form
     */
    private static void setPostParams(HttpPost httpPost, Map<String, Object> params) {
        List<NameValuePair> nvps = new ArrayList<>();
        for (Map.Entry<String, Object> e :params.entrySet()) {
            nvps.add(new BasicNameValuePair(e.getKey(), String.valueOf(e.getValue())));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));
    }

    /**
     * 设置 get 请求的参数
     *
     * @param httpGet get 对象
     * @param uri     网址
     * @param params  参数
     */
    private static void setGetParams(HttpGet httpGet, String uri, Map<String, Object> params) {
        try {
            URIBuilder builder = new URIBuilder(uri);
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, Object> e :
                        params.entrySet()) {
                    builder.setParameter(e.getKey(), String.valueOf(e.getValue()));
                }
            }
            httpGet.setURI(builder.build());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static void setHeader(HttpMessage httpMessage, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                httpMessage.addHeader(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 关闭连接池
     */
    public static void closeConnectionPool() {
        try {
            httpClient.close();
            manager.close();
            monitorExecutor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
