package com.hh.function.http.useragent.policy;

import com.hh.utils.AssertUtils;
import com.hh.utils.CheckUtils;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;

/**
 * @author ab875
 * map 需要包含 INDEX 字段
 */
@Data
public class IndexUserAgentPolicy implements UserAgentPolicy {
    public static final String INDEX = "index";
    private final Logger logger = LogManager.getLogger(IndexUserAgentPolicy.class);
    private Map<String, Object> args;

    @Override
    public String getUserAgent(List<String> userAgents) {
        int size = userAgents.size();
        if (!CheckUtils.checkArgs(userAgents, args)) {
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
        return userAgents.get(Math.abs(index) % size);
    }
}
