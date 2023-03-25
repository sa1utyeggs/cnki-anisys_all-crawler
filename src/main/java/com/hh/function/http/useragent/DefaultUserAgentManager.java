package com.hh.function.http.useragent;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hh.function.http.useragent.policy.UserAgentPolicy;
import com.hh.utils.JsonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ab875
 */
@Data
public class DefaultUserAgentManager implements InitializingBean, UserAgentManager {

    private final Logger logger = LogManager.getLogger(DefaultUserAgentManager.class);
    private String fileName;
    private List<String> userAgents;
    private UserAgentPolicy policy;

    @Override
    public String getUserAgent() {
        // 保证下标正确
        return policy.getUserAgent(userAgents);
    }

    @Override
    public void addUserAgent(String sUserAgent) {
        userAgents.add(sUserAgent);
    }

    @Override
    public void removeUserAgent(String sUserAgent) {
        userAgents.remove(sUserAgent);
    }

    @Override
    public String getDefaultUserAgent() {
        return policy.getDefaultUserAgent(userAgents);
    }

    private void init() {
        try {
            JSONObject json = JsonUtils.getJsonObjectFromFile(fileName);
            JSONArray array = json.getJSONArray("list");

            // 初始化
            userAgents = new ArrayList<>(array.size());
            for (Object o : array) {
                userAgents.add((String) o);
            }
        } catch (IOException e) {
            logger.error("初始化 user-agent 失败");
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }
}
