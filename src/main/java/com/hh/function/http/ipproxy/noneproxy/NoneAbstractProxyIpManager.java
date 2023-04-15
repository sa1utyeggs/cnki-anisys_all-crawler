package com.hh.function.http.ipproxy.noneproxy;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hh.function.http.ipproxy.IpPool;
import com.hh.function.http.ipproxy.ProxyIp;
import com.hh.function.http.ipproxy.ProxyIpManager;
import com.hh.function.http.ipproxy.xiaoxiang.XiaoxiangConfig;
import com.hh.utils.DateUtils;
import lombok.Data;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author 86183
 * 单线程情况下的 IP管理
 */
@Data
public abstract class NoneAbstractProxyIpManager implements ProxyIpManager, InitializingBean {
    protected Logger logger;

    public NoneAbstractProxyIpManager() {}

    /**
     * 不使用代理
     */
    @Override
    public void initIpPool() {
        logger.info("use no proxy");
    }

    /**
     * Bean 初始化结束后，初始化代理 IP 池
     *
     * @throws Exception e
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        initIpPool();
    }

}
