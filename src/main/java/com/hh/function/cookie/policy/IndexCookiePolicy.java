package com.hh.function.cookie.policy;

import com.hh.utils.AssertUtils;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * @author ab875
 * map 需要包含 INDEX 字段
 */
@Data
public class IndexCookiePolicy implements CookiePolicy {
    public static final String INDEX = "index";
    private final Logger logger = LogManager.getLogger(IndexCookiePolicy.class);
    private Map<String, Object> args;

    @Override
    public String getCookie(List<String> cookies) {
        int size = cookies.size();
        if (checkArgs(cookies, args)) {
            return null;
        }
        Integer index = 0;
        // 获取 index
        try {
            index = (Integer) args.get(INDEX);
        } catch (Exception e) {
            logger.error("index 获取失败");
            e.printStackTrace();
        }

        // 保证
        AssertUtils.sysIsError(size == 0,"cookie 为空");
        return cookies.get(Math.abs(index) % size);
    }
}
