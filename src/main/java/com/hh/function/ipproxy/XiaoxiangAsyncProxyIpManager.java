package com.hh.function.ipproxy;

import com.hh.function.system.ThreadPoolFactory;

import java.io.IOException;

/**
 * @author 86183
 */
public class XiaoxiangAsyncProxyIpManager extends XiaoxiangProxyIpManager {
    private final Object sync;

    public XiaoxiangAsyncProxyIpManager() {
        super();
        this.sync = new Object();
    }

    @Override
    public ProxyIp getIp() {
        // 根据 线程 Id 获得 对应的
        ProxyIp proxyIp = ipPool.getIpRandomly();
        if (proxyIp == null) {
            synchronized (sync) {
                // recheck
                proxyIp = ipPool.getIpRandomly();
                while (proxyIp == null) {
                    System.out.println("无可用 IP，尝试重新初始化代理 IP 池");
                    try {
                        initIpPool();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    proxyIp = ipPool.getIpRandomly();
                }
            }
        }
        return proxyIp;
    }
}
