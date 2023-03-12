package com.hh.function.http.cookie.policy;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

/**
 * @author ab875
 * 传入参数时，map 需要包含 INDEX 字段
 */
@Data
public class RandomCookiePolicy implements CookiePolicy {
    private final Random random;
    private final Logger logger = LogManager.getLogger(RandomCookiePolicy.class);

    public RandomCookiePolicy() {
        this.random = new Random();
    }

    @Override
    public String getCookie(List<String> cookies) {
        if (checkArgs(cookies)) {
            return null;
        }
        // 返回
        return cookies.get(random.nextInt(cookies.size()));
    }
}
