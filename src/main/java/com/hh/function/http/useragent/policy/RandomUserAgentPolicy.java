package com.hh.function.http.useragent.policy;

import com.hh.utils.CheckUtils;
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
public class RandomUserAgentPolicy implements UserAgentPolicy {
    private final Random random;
    private final Logger logger = LogManager.getLogger(RandomUserAgentPolicy.class);

    public RandomUserAgentPolicy() {
        this.random = new Random();
    }

    @Override
    public String getUserAgent(List<String> cookies) {
        if (!CheckUtils.checkArgs(cookies)) {
            return null;
        }
        // 返回
        return cookies.get(random.nextInt(cookies.size()));
    }
}
