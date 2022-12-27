package com.hh.function.proxy;

import java.io.IOException;

/**
 * @author 86183
 */
public interface IpProxy {
    /**
     * 获得单条 IP
     * @return IP
     */
    public ProxyIp getIp();

    /**
     * 获得代理 IP 池
     */
    public void initIpPool() throws IOException;
}
