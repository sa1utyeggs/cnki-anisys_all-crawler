package com.hh.entity.application.test;

import com.hh.entity.application.Task;
import com.hh.function.base.ContextSingletonFactory;
import com.hh.function.http.HttpConnectionPool;
import com.hh.utils.HttpUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author ab875
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TestTask extends Task implements Callable<CloseableHttpResponse> {
    /**
     * const
     */
    public static final String GET = "get";
    public static final String POST = "post";
    private final Logger logger = LogManager.getLogger(TestTask.class);

    /**
     * HTTP 参数
     */
    private Long id;
    private String url1;
    private String url2;
    private java.util.Map<String, Object> params;
    private Map<String, String> excessHeaders;
    private String method;

    /**
     * spring
     */
    private static final ApplicationContext CONTEXT = ContextSingletonFactory.getInstance();
    private static final HttpConnectionPool HTTP_CONNECTION_POOL = CONTEXT.getBean("httpConnectionPool", HttpConnectionPool.class);


    public TestTask(Long id, String url1,String url2, Map<String, Object> params, Map<String, String> excessHeaders, String method) {
        super();
        this.id = id;
        this.url1 = url1;
        this.url2 = url2;
        this.params = params;
        this.excessHeaders = excessHeaders;
        this.method = method;
    }


    @Override
    public CloseableHttpResponse call() {
        CloseableHttpResponse response = null;
        try {
            // 打印第二次
            logger.info("task(" + id + ":1) start");
            // 发送请求
            response = HTTP_CONNECTION_POOL.test(url1, method, params, excessHeaders, this);
            logger.info("response headers: " + Arrays.toString(response.getAllHeaders()));
            logger.info("document: ");
            System.out.println(HttpUtils.getDocument(response).text().substring(0, 200) + "\n" + ".......");
            logger.info("task(" + id + ":1) end");

            // 下一个请求需要使用上一个 Cookie 和一些新的 Cookie；
            // 获得 Set-Cookie 字段
            Header[] setCookies = response.getHeaders("Set-Cookie");
            response.close();
            StringBuilder cookie = new StringBuilder(super.getHeader("Cookie"));
            // 拼接 Cookie
            for (Header setCookie : setCookies) {
                cookie.append(setCookie).append(";");
            }
            excessHeaders.put("Cookie", cookie.toString());



            // 执行第二次任务
            logger.info("task(" + id + ":2) start");
            // 发送请求
            response = HTTP_CONNECTION_POOL.test(url2, method, params, excessHeaders, this);
            logger.info("response headers: " + Arrays.toString(response.getAllHeaders()));
            logger.info("document: ");
            System.out.println(HttpUtils.getDocument(response).text().substring(0, 200) + "\n" + ".......");
            logger.info("task(" + id + ":1) end");
            response.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

}
