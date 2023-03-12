package com.hh.function.http.useragent.policy;

import com.hh.utils.CheckUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author ab875
 */
public interface UserAgentPolicy {

    /**
     * 获取 cookie
     *
     * @param userAgents collection
     * @return String
     */
    public String getUserAgent(List<String> userAgents);

    /**
     * 获得默认的 cookie
     * @param userAgents array
     * @return string
     */
    public default String getDefaultUserAgent(List<String> userAgents){
        if (!CheckUtils.checkArgs(userAgents)){
            return null;
        }
        return userAgents.get(0);
    }

}
