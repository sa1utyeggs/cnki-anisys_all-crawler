package com.hh.function.ipproxy;

import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;

/**
 * @author 86183
 */
public interface ProxyIpManager {
    /**
     * 获得单条 IP
     * @return IP
     */
    public ProxyIp getIp();

    /**
     * 初始化 代理 IP 池
     * @throws Exception 异常
     */
    public void initIpPool() throws Exception;
}
