package com.hh.function;

import cn.hutool.core.util.URLUtil;
import com.hh.utils.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.File;
import java.net.URL;
import java.util.Map;

/**
 * @author 86183
 */
public class Base {
    public static Connection getCnkiConnection(String url) throws Exception {
        Connection con;
        try {
            con = Jsoup.connect(url);
            con.header("Accept", "text/html, */*; q=0.01");
            con.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            con.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36");
            con.header("Host", "kns.cnki.net");
            con.header("Origin", "https://kns.cnki.net");
            URL resource = JsonUtils.class.getResource("/");
            if (resource != null) {
                String baseUrl = resource.getPath();
                File cookieFile = new File(baseUrl + "cookie.txt");
                String cookie;

                // 从文件里读 cookie
                cookie = FileUtils.readFileToString(cookieFile, "utf-8");
                // 注意替换 ; / ? : @ = &
                cookie = URLUtil.normalize(cookie);
                con.header("Cookie", cookie);

            }
            return con;
        } catch (Exception e) {
            System.out.println("获得Cnki连接失败");
            throw e;
        }
    }

    public static void insertPostData(Map<String, Object> data, Connection connection) {
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
