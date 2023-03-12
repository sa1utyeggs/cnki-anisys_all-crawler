package com.hh.function.http.ipproxy;

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
