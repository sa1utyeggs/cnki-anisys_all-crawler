package com.hh.utils;

import cn.hutool.core.util.URLUtil;
import com.hh.entity.system.HttpTask;
import com.hh.function.ipproxy.ProxyIp;
import com.hh.function.ipproxy.ProxyIpManager;
import com.hh.function.ipproxy.XiaoxiangProxyIpManager;
import com.hh.function.system.ContextSingletonFactory;
import com.hh.function.system.ThreadPoolFactory;
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
import org.apache.http.config.SocketConfig;
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
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.concurrent.*;


/**
 * @author 86183
 */
public class HttpConnectionPoolUtils {

    private static final Logger logger = LogManager.getLogger(HttpConnectionPoolUtils.class);

    /**
     * 指客户端和服务器建立连接的 超时时间
     */
    private static final int CONNECT_TIMEOUT = 10000;
    /**
     * 指从 连接池里拿出连接的超时时间
     */
    private static final int CONNECTION_REQUEST_TIMEOUT = 2000;
    /**
     * 指客户端和服务器建立连接后，客户端从服务器读取数据的 timeout
     */
    private static final int SOCKET_TIMEOUT = 10000;

    /**
     * 指 MAX_TOTAL：连接池有个最大连接数
     */
    private static final int MAX_CONN = 10;
    /**
     * 每个 route 对应一个小连接池，也有个最大连接数
     */
    private static final int MAX_PER_ROUTE = 2;
    /**
     * route 最大个数
     */
    private static final int MAX_ROUTE = 5;
    /**
     * 空闲线程的存活时间
     */
    private static final int IDLE_TIMEOUT = 10_000;
    /**
     * 空闲线程检查时间
     */
    private static final int MONITOR_INTERVAL = 20_000;

    /**
     * 重试的时间间隔
     */
    private static final long RETRY_INTERVAL = 1000;

    /**
     * 当线程卡死重试次数
     */
    public static final int CRASH_RETRY_TIMES = 1;

    /**
     * 判定请求卡死的超时时间
     */
    private static final int KILL_THREAD_TIMEOUT = MONITOR_INTERVAL * 3;

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
    private static final ApplicationContext CONTEXT = ContextSingletonFactory.getInstance();
    private static final ProxyIpManager IP_PROXY = CONTEXT.getBean("xiaoxiangProxyIpManager", XiaoxiangProxyIpManager.class);
    private static final ThreadPoolFactory THREAD_POOL_FACTORY = CONTEXT.getBean("threadPoolFactory", ThreadPoolFactory.class);
    private static final ExecutorService HTTP_THREAD_POOL = THREAD_POOL_FACTORY.getThreadPool(ThreadPoolFactory.HTTP_CONNECTION_POOL_PREFIX);


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
        // 获取代理 IP
        ProxyIp ip = IP_PROXY.getIp();
        RequestConfig requestConfig = RequestConfig.custom()
                // 指三次握手的超时时间
                .setConnectTimeout(CONNECT_TIMEOUT)
                // 指 从 connectManager 连接池中获取 Connection 超时时间
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                // 指连接上后，接收数据的超时时间
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
        if ("https:".equals(split[0])) {
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
                    // 开启监控线程，对异常和空闲线程进行关闭
                    monitorExecutor = Executors.newScheduledThreadPool(1);
                    monitorExecutor.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            logger.warn("清理前：打印连接池状态：" + manager.getTotalStats());
                            // 关闭过期连接
                            manager.closeExpiredConnections();
                            // 关闭空闲连接
                            manager.closeIdleConnections(IDLE_TIMEOUT, TimeUnit.MILLISECONDS);
                            logger.warn("closing expired and idle for over " + IDLE_TIMEOUT / 1000 + "s connection done");
                            logger.warn("清理后：打印连接池状态：" + manager.getTotalStats());
                            logger.warn("打印 HTTP 线程池状态：" + HTTP_THREAD_POOL.toString());
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


        HttpHost httpHost = new HttpHost(host, port);
        manager.setMaxPerRoute(new HttpRoute(httpHost), MAX_ROUTE);


        // 请求失败时,进行请求重试
        HttpRequestRetryHandler handler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                if (i > 2) {
                    //重试超过 2 次,放弃请求
                    logger.error("retry has more than 2 time, give up request");
                    return false;
                }
                if (e instanceof NoHttpResponseException) {
                    // 服务器没有响应,可能是服务器断开了连接,应该重试
                    logger.error("receive no response from server, retry");
                    return true;
                }
                if (e instanceof SSLHandshakeException) {
                    // SSL握手异常
                    logger.error("SSL hand shake exception, no retry");
                    return false;
                }
                if (e instanceof InterruptedIOException) {
                    // 超时
                    logger.error("InterruptedIOException, no retry");
                    return false;
                }
                if (e instanceof UnknownHostException) {
                    // 服务器不可达
                    logger.error("server host unknown, no retry");
                    return false;
                }
                if (e instanceof SSLException) {
                    logger.error("SSLException, retry");
                    return true;
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

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoKeepAlive(true)
                // 按照 https://blog.csdn.net/xs_challenge/article/details/109737264 的说法
                // 由于使用了 Proxy 的 getDefaultSocketConfig 的超时时间为 0
                // 所以重新设置 SocketTimeout 可以解决问题
                // https://www.jianshu.com/p/5e1bdfc992b9 中也提到这种方法
                // 但是，问题依旧没有解决
                .setSoTimeout(SOCKET_TIMEOUT)
                .build();

        return HttpClients.custom()
                .setDefaultSocketConfig(socketConfig)
                .setConnectionManager(manager)
                .setRetryHandler(handler)
                .build();
    }

    /**
     * 发送 get 请求
     *
     * @param url    URL
     * @param params 参数
     * @return Document
     */
    public static Document get(String url, Map<String, Object> params, Map<String, String> excessHeaders) throws Exception {
        HttpGet httpGet = new HttpGet();
        // 基础 header
        setHeader(httpGet, BASE_HEADERS);
        // 额外 header
        setHeader(httpGet, excessHeaders);
        setRequestConfig(httpGet);
        // 设置参数
        setGetParams(httpGet, url, params);

        return executeWithRetry(httpGet, url, CRASH_RETRY_TIMES, RETRY_INTERVAL);
    }

    /**
     * 发送 post 请求
     *
     * @param url    URL
     * @param params 参数
     * @return Document
     */
    public static Document post(String url, Map<String, Object> params, Map<String, String> excessHeaders) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        // 基础 header
        setHeader(httpPost, BASE_HEADERS);
        // 额外 header
        setHeader(httpPost, excessHeaders);
        setRequestConfig(httpPost);
        // form 表单
        setPostParams(httpPost, params);

        return executeWithRetry(httpPost, url, CRASH_RETRY_TIMES, RETRY_INTERVAL);
    }

    /**
     * 设置 post 请求的参数
     *
     * @param httpPost post
     * @param params   form
     */
    private static void setPostParams(HttpPost httpPost, Map<String, Object> params) {
        List<NameValuePair> nvps = new ArrayList<>();
        for (Map.Entry<String, Object> e : params.entrySet()) {
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
    private static void setGetParams(HttpGet httpGet, String uri, Map<String, Object> params) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(uri);
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> e :
                    params.entrySet()) {
                builder.setParameter(e.getKey(), String.valueOf(e.getValue()));
            }
        }
        httpGet.setURI(builder.build());
    }

    private static void setHeader(HttpMessage httpMessage, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                httpMessage.addHeader(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 使用线程池执行 HTTP 请求
     *
     * @param request 请求
     * @param url     网址
     * @return Document
     * @throws Exception e
     */
    private static Document execute(HttpRequestBase request, String url) throws Exception {
        Document document = null;
        HttpTask task = null;
        try {
            InputStream in = null;
            // 使用线程池提交请求
            CloseableHttpResponse response = HTTP_THREAD_POOL.submit((task = new HttpTask(request, getHttpClient(url)))).get(KILL_THREAD_TIMEOUT, TimeUnit.MILLISECONDS);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    in = entity.getContent();
                    document = Jsoup.parse(IOUtils.toString(in, StandardCharsets.UTF_8));
                    // 消费 entity
                    EntityUtils.consume(entity);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (response != null) {
                    // 将 response 关闭后，就能将连接放回连接池
                    response.close();
                }
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // 在外部关闭连接
            task.abortRequest();
            logger.error("任务 " + task + " 最终超时，关闭连接并抛出异常");
            throw e;
        }
        return document;
    }


    private static Document executeWithRetry(HttpRequestBase request, String url, int retry, long during) throws Exception {
        int curr = Math.max(0, retry);
        Document document = null;
        Exception thr = null;
        while (document == null && curr-- >= 0) {
            try {
                document = execute(request, url);
            } catch (Exception e) {
                thr = e;
                // 如果立刻重试，大概率会失败
                Thread.sleep(during);
            }
        }
        if (document == null) {
            logger.error("重复执行 HTTP 请求无效，抛出异常");
            throw thr != null ? thr : new Exception("无法获得结果，且未捕捉到异常");
        }
        return document;

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
