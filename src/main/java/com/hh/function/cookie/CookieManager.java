package com.hh.function.cookie;

import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hh.function.cookie.policy.CookiePolicy;
import com.hh.utils.FileUtils;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ab875
 * Cookie 控制类
 */
@Data
public class CookieManager {
    private final Logger logger = LogManager.getLogger(CookieManager.class);
    private String fileName;
    private List<String> cookies;
    private CookiePolicy policy;

    public CookieManager() {
        try {
            // 读取文件
            String sCookie = FileUtils.readFromResource(fileName);
            // 规范化 cookie
            sCookie = URLUtil.normalize(sCookie);
            JSONObject json = JSON.parseObject(sCookie);
            JSONArray array = json.getJSONArray("list");

            // 初始化
            cookies = new ArrayList<>(array.size());
            for (Object o : array) {
                cookies.add((String) o);
            }
        } catch (IOException e) {
            logger.error("初始化 cookie 失败");
            e.printStackTrace();
        }
    }

    public String getCookie() {
        // 保证下标正确
        return policy.getCookie(cookies);
    }

    public String getDefaultCookie() {
        return policy.getDefaultCookie(cookies);
    }

}
