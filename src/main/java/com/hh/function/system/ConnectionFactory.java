package com.hh.function.system;

import cn.hutool.core.util.URLUtil;
import com.hh.function.ipproxy.ProxyIpManager;
import com.hh.function.ipproxy.ProxyIp;
import com.hh.utils.JsonUtils;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.File;
import java.net.URL;
import java.util.Map;

/**
 * @author 86183
 */
@Data
public class ConnectionFactory {
    private String cookie;
    private ProxyIpManager proxyIpManager;

    public ConnectionFactory() {
        try {
            // 初始化 cookie
            URL resource = JsonUtils.class.getResource("/");
            if (resource != null) {
                String baseUrl = resource.getPath();
                File cookieFile = new File(baseUrl + "cookie.txt");
                // 从文件里读 cookie
                cookie = FileUtils.readFileToString(cookieFile, "utf-8");
                // 注意替换 ; / ? : @ = &
                cookie = URLUtil.normalize(cookie);
            }

            // 由 Spring 初始化 代理 IP 池
        } catch (Exception e) {
            System.out.println("ConnectionFactory：初始化失败");
        }
    }

    /**
     * 获得 指定知网 url 的连接
     *
     * @param url 知网 url
     * @return 连接对象
     */
//    public Connection getCnkiConnection(String url) {
//        return getCnkiConnection(url, true);
//    }

    /**
     * 获得 指定知网 url 的连接
     *
     * @param url   知网 url
     * @param proxy 是否使用代理 IP
     * @return 连接对象
     */
    public Connection getCnkiConnection(String url, boolean proxy) {
        Connection con;
        try {
            con = Jsoup.connect(url);
            con.header("Accept", "text/html, */*; q=0.01");
            con.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            con.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36");
            con.header("Host", "kns.cnki.net");
            con.header("Origin", "https://kns.cnki.net");
            con.header("Cookie", cookie);
            con.header("Connection", "keep-alive");



            if (proxy) {
                ProxyIp ip = proxyIpManager.getIp();
                con.proxy(ip.getIp(), ip.getPort());
            }
            return con;
        } catch (Exception e) {
            System.out.println("获得Cnki连接失败");
            throw e;
        }
    }

    /**
     * 插入 Post 请求的 form data
     *
     * @param data       form
     * @param connection 连接对象
     */
    public void insertPostData(Map<String, Object> data, Connection connection) {
        // 循环插入数据
        try {
            for (Map.Entry<String, Object> d : data.entrySet()) {
                // 强制转换为字符串
                connection.data(d.getKey(), String.valueOf(d.getValue()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
