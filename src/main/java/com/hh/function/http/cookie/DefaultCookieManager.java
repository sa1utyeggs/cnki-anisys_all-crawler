package com.hh.function.http.cookie;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hh.function.http.cookie.policy.CookiePolicy;
import com.hh.utils.FileUtils;
import lombok.Data;
import org.apache.http.cookie.Cookie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ab875
 * Cookie 控制类
 * 使用策略模式实现 cookie 的获取
 */
@Data
public class DefaultCookieManager implements InitializingBean, CookieManager {
    private final Logger logger = LogManager.getLogger(DefaultCookieManager.class);
    private String fileName;
    private List<String> cookies;
    private CookiePolicy policy;

    @Override
    public String getCookie() {
        // 保证下标正确
        return policy.getCookie(cookies);
    }

    @Override
    public void addCookie(String sCookie){
        cookies.add(sCookie);
    }

    @Override
    public void removeCookie(String sCookie){
        cookies.remove(sCookie);
    }

    @Override
    public String getDefaultCookie() {
        return policy.getDefaultCookie(cookies);
    }

    private void init(){
        try {
            // 读取文件
            String sCookie = FileUtils.readFromResource(fileName);
            // 规范化 cookie
            // sCookie = URLUtil.normalize(sCookie);
            JSONObject json = JSONObject.parseObject(sCookie);
            JSONArray array = json.getJSONArray("list");

            // 初始化
            cookies = new ArrayList<>(array.size());
            for (Object o : array) {
                cookies.add((String) ((JSONObject) o).get("text"));
            }
        } catch (IOException e) {
            logger.error("初始化 cookie 失败");
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }
}
