package com.hh.function.http.ipproxy.fixed;

import com.hh.function.http.ipproxy.ProxyIp;
import com.hh.function.http.ipproxy.ProxyIpManager;
import lombok.Data;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author 86183
 * 单线程情况下的 IP管理
 */
@Data
public abstract class FixedAbstractProxyIpManager implements ProxyIpManager, InitializingBean {
    protected Logger logger;
    private String host;
    private int port;
    private ProxyIp proxyIp;

    public FixedAbstractProxyIpManager() {}

    /**
     * 不使用代理
     */
    @Override
    public void initIpPool() {
        logger.info("no proxy pool");
    }

    public void init(){
        this.proxyIp = ProxyIp.builder().ip(host).port(port).build();
    }

    /**
     * Bean 初始化结束后，初始化代理 IP 池
     *
     * @throws Exception e
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

}
