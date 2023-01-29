package com.hh.function.system;

import cn.hutool.core.util.URLUtil;
import com.hh.entity.system.HttpTask;
import com.hh.function.ipproxy.AsyncProxyIpManager;
import com.hh.function.ipproxy.ProxyIp;
import com.hh.function.ipproxy.ProxyIpManager;
import com.hh.function.ipproxy.SyncProxyIpManager;
import com.hh.utils.JsonUtils;
import lombok.Data;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;


/**
 * @author 86183
 */
@Data
public class HttpConnectionPool implements InitializingBean {

    private final Logger logger = LogManager.getLogger(HttpConnectionPool.class);

    /**
     * 指客户端和服务器建立连接的 超时时间
     */
    private int connectionTimeout = 10000;
    /**
     * 指从 连接池里拿出连接的超时时间
     */
    private int connectionRequestTimeout = 2000;
    /**
     * 指客户端和服务器建立连接后，客户端从服务器读取数据的 timeout
     */
    private int socketTimeout = 10000;

    /**
     * 指 MAX_TOTAL：连接池有个最大连接数
     */
    private int maxConnectionNum = 10;
    /**
     * 每个 route 对应一个小连接池，也有个最大连接数
     */
    private int maxConnectionPerRoute = 2;
    /**
     * route 最大个数
     */
    private int maxRoute = 5;
    /**
     * 空闲线程的存活时间
     */
    private int idleTimeout = 10_000;
    /**
     * 空闲线程检查时间
     */
    private int monitorInterval = 20_000;

    /**
     * 重试的时间间隔
     */
    private long retryInterval = 1000;

    /**
     * 当线程卡死重试次数
     */
    public int crashRetryTimes = 1;

    /**
     * 判定请求卡死的超时时间
     */
    private int killThreadTimeout = monitorInterval * 3;

    /**
     * 发送请求的客户端单例
     */
    private volatile CloseableHttpClient httpClient;
    /**
     * 连接池管理类
     */
    private PoolingHttpClientConnectionManager manager;
    private ScheduledExecutorService monitorExecutor;

    /**
     * 线程锁，用于线程安全
     */
    private final Object SYNC_LOCK = new Object();

    /**
     * 请求 header 与 cookie
     */
    public final Map<String, String> BASE_HEADERS = new HashMap<>(16);
    private String cookie;

    /**
     * Spring 容器 Bean
     */
    private ProxyIpManager proxyIpManager;
    private ThreadPoolFactory threadPoolFactory;
    /**
     * http 请求执行线程池
     */
    private ExecutorService httpThreadPool;

    public HttpConnectionPool() {

        // 初始化 cookie
        try {
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
            logger.error("初始化 cookie 失败");
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
     * 是否开启多线程；<br/>
     * 该方法只能在 threadPoolFactory 初始化完成后调用；<br/>
     *
     * @return async
     */
    public boolean isAsync() {
        return threadPoolFactory.getThreadNum() == 1;
    }

    /**
     * 对http请求进行基本设置；<br/>
     * 该方法调用 proxyIpManager.getIp() 方法，获取 ip；<br/>
     * 在 proxyIpManager.getIp() 线程安全的前提下，该方法线程安全；<br/>
     *
     * @param httpRequestBase http请求
     */
    private void setRequestConfig(HttpRequestBase httpRequestBase) {
        // 获取代理 IP
        ProxyIp ip = proxyIpManager.getIp();
        RequestConfig requestConfig = RequestConfig.custom()
                // 指三次握手的超时时间
                .setConnectTimeout(connectionTimeout)
                // 指 从 connectManager 连接池中获取 Connection 超时时间
                .setConnectionRequestTimeout(connectionRequestTimeout)
                // 指连接上后，接收数据的超时时间
                .setSocketTimeout(socketTimeout)
                // 设置代理
                .setProxy(new HttpHost(ip.getIp(), ip.getPort()))
                .build();
        httpRequestBase.setConfig(requestConfig);
    }

    /**
     * 获取 HttpClient 单例；<br/>
     * 该方法线程安全；<br/>
     *
     * @param url
     * @return
     */
    @SuppressWarnings({"all"})
    public CloseableHttpClient getHttpClient(String url) {
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
                            logger.warn("清理前：连接池状态：" + manager.getTotalStats());
                            // 关闭过期连接
                            manager.closeExpiredConnections();
                            // 关闭空闲连接
                            manager.closeIdleConnections(idleTimeout, TimeUnit.MILLISECONDS);
                            logger.warn("closing expired and idle for over " + idleTimeout / 1000 + "s connection done");
                            logger.warn("清理后：连接池状态：" + manager.getTotalStats());
                            logger.warn("HTTP 线程池状态：" + httpThreadPool.toString());
                        }
                    }, monitorInterval, monitorInterval, TimeUnit.MILLISECONDS);
                }
            }
        }
        return httpClient;
    }

    /**
     * 根据 host 和 port 构建httpclient实例；<br/>
     * 该方法线程不安全<br/>
     *
     * @param host 要访问的域名
     * @param port 要访问的端口
     * @return httpclient
     */
    public CloseableHttpClient createHttpClient(String host, int port) {
        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", plainSocketFactory)
                .register("https", sslSocketFactory).build();

        manager = new PoolingHttpClientConnectionManager(registry);

        // 最大连接数
        manager.setMaxTotal(maxConnectionNum);
        // 路由最大连接数
        manager.setDefaultMaxPerRoute(maxConnectionPerRoute);


        HttpHost httpHost = new HttpHost(host, port);
        manager.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);


        // 请求失败时,进行请求重试
        HttpRequestRetryHandler handler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                boolean retry = false;
                if (i > crashRetryTimes) {
                    //重试超过 crashRetryTimes 次,放弃请求
                    logger.error("retry has more than " + crashRetryTimes + " time, give up request");
                    return false;
                }
                logger.warn("尚未到达重试上线" + i + "/" + crashRetryTimes + "，HttpRequestRetryHandler 尝试重试");
                if (e instanceof NoHttpResponseException) {
                    // 服务器没有响应,可能是服务器断开了连接,应该重试
                    logger.error("receive no response from server, retry");
                    retry = true;
                } else if (e instanceof SSLHandshakeException) {
                    // SSL握手异常
                    logger.error("SSL hand shake exception, no retry");
                } else if (e instanceof InterruptedIOException) {
                    // 主动打断 SocketIO，可能是使用了 abort 方法
                    logger.error("InterruptedIOException, retry");
                    retry = true;
                } else if (e instanceof UnknownHostException) {
                    // 服务器不可达
                    logger.error("server host unknown, no retry");
                } else if (e instanceof SSLException) {
                    logger.error("SSLException, retry");
                    retry = true;
                }

                if (!(HttpClientContext.adapt(httpContext).getRequest() instanceof HttpEntityEnclosingRequest)) {
                    // 如果请求不是关闭连接的请求
                    retry = true;
                }
                logger.warn("HttpRequestRetryHandler 是否重试：" + retry);
                return retry;
            }
        };

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoKeepAlive(true)
                // 按照 https://blog.csdn.net/xs_challenge/article/details/109737264 的说法
                // 由于使用了 Proxy 的 getDefaultSocketConfig 的超时时间为 0
                // 所以重新设置 SocketTimeout 可以解决问题
                // https://www.jianshu.com/p/5e1bdfc992b9 中也提到这种方法
                // 但是，问题依旧没有解决
                .setSoTimeout(socketTimeout)
                .build();

        return HttpClients.custom()
                .setDefaultSocketConfig(socketConfig)
                .setConnectionManager(manager)
                .setRetryHandler(handler)
                .build();
    }

    /**
     * 发送 get 请求；<br/>
     *
     * @param url    URL
     * @param params 参数
     * @return Document
     */
    public Document get(String url, Map<String, Object> params, Map<String, String> excessHeaders) throws Exception {
        HttpGet httpGet = new HttpGet();
        // 基础 header
        setHeader(httpGet, BASE_HEADERS);
        // 额外 header
        setHeader(httpGet, excessHeaders);
        setRequestConfig(httpGet);
        // 设置参数
        setGetParams(httpGet, url, params);

        return executeWithRetry(httpGet, url, crashRetryTimes, retryInterval);
    }

    /**
     * 发送 post 请求；<br/>
     *
     * @param url    URL
     * @param params 参数
     * @return Document
     */
    public Document post(String url, Map<String, Object> params, Map<String, String> excessHeaders) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        // 基础 header
        setHeader(httpPost, BASE_HEADERS);
        // 额外 header
        setHeader(httpPost, excessHeaders);
        setRequestConfig(httpPost);
        // form 表单
        setPostParams(httpPost, params);

        return executeWithRetry(httpPost, url, crashRetryTimes, retryInterval);
    }

    /**
     * 设置 post 请求的参数；<br/>
     * 该方法线程安全；<br/>
     *
     * @param httpPost post
     * @param params   form
     */
    private void setPostParams(HttpPost httpPost, Map<String, Object> params) {
        List<NameValuePair> nvps = new ArrayList<>();
        for (Map.Entry<String, Object> e : params.entrySet()) {
            nvps.add(new BasicNameValuePair(e.getKey(), String.valueOf(e.getValue())));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));
    }

    /**
     * 设置 get 请求的参数；<br/>
     * 该方法线程安全；<br/>
     *
     * @param httpGet get 对象
     * @param uri     网址
     * @param params  参数
     */
    private void setGetParams(HttpGet httpGet, String uri, Map<String, Object> params) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(uri);
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> e :
                    params.entrySet()) {
                builder.setParameter(e.getKey(), String.valueOf(e.getValue()));
            }
        }
        httpGet.setURI(builder.build());
    }

    /**
     * 设置请求对象的 header；<br/>
     *
     * @param httpMessage HttpGet/HttpPost ...
     * @param headers     header
     */
    private void setHeader(HttpMessage httpMessage, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                httpMessage.addHeader(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 使用线程池执行 HTTP 请求；<br/>
     *
     * @param request 请求
     * @param url     网址
     * @return Document
     * @throws Exception e
     */
    private Document execute(HttpRequestBase request, String url) throws Exception {
        Document document = null;
        HttpTask task = null;
        try {
            InputStream in = null;
            // 使用线程池提交请求
            CloseableHttpResponse response = httpThreadPool.submit((task = new HttpTask(request, getHttpClient(url)))).get(killThreadTimeout, TimeUnit.MILLISECONDS);
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
            // 在外部关闭连接，不然可能会出现线程卡死在 readSocket0() 的问题
            task.abortRequest();
            logger.error("任务 " + task + " 最终超时，关闭连接并抛出异常");
            throw e;
        }
        return document;
    }


    /**
     * 该方法主要是为了捕捉到 HttpRequestRetryHandler 无法捕捉到的非网络因素异常；<br/>
     *
     * @param request request
     * @param url     url
     * @param retry   重试次数
     * @param during  重试间隔
     * @return Document
     * @throws Exception e
     */
    private Document executeWithRetry(HttpRequestBase request, String url, int retry, long during) throws Exception {
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
            logger.error("外部判断逻辑重试后依旧失败，抛出异常");
            throw thr != null ? thr : new Exception("无法获得结果，且未捕捉到异常");
        }
        return document;

    }

    /**
     * 关闭连接池；<br/>
     */
    public void closeConnectionPool() {
        try {
            httpClient.close();
            manager.close();
            monitorExecutor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 由于 Spring 容器特性，需要重写该方法保证属性的注入
     */
    @Override
    public void afterPropertiesSet() {
        // 初始化 HTTP pool
        httpThreadPool = threadPoolFactory.getThreadPool(ThreadPoolFactory.HTTP_CONNECTION_POOL_PREFIX);
    }
}
