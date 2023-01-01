package com.hh.function.ipproxy.xiaoxiang;

import com.hh.function.ipproxy.AsyncProxyIpManager;
import com.hh.function.ipproxy.ProxyIp;
import com.hh.function.system.ThreadPoolFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 86183
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class XiaoxiangAsyncProxyIpManager extends XiaoxiangAbstractProxyIpManager implements AsyncProxyIpManager {
    private final ReentrantLock lock;
    private boolean getIpRandomly;

    public XiaoxiangAsyncProxyIpManager() {
        super();
        this.lock = new ReentrantLock();
        this.logger = LogManager.getLogger(XiaoxiangAsyncProxyIpManager.class);
    }

    /**
     * 根据当前线程 name 的最后的数字来分配 ip
     *
     * @return ip
     */
    @Override
    public ProxyIp getIp() {
        // 根据 线程 Id 获得 对应的 IP
        ProxyIp proxyIp = doGetIp();
        if (!ProxyIp.isValid(proxyIp)) {
            lock.lock();
            try {
                // recheck
                proxyIp = doGetIp();
                while (!ProxyIp.isValid(proxyIp)) {
                    logger.warn("获取 IP 失败，尝试重新初始化代理 IP 池");
                    try {
                        initIpPool();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    proxyIp = doGetIp();
                }
            } finally {
                lock.unlock();
            }
        }
        return proxyIp;
    }

    /**
     * @return ip
     */
    private ProxyIp doGetIp() {
        return getIpRandomly ? ipPool.getIpRandomly() : ipPool.getIp(ThreadPoolFactory.getThreadId());
    }

    /**
     * Bean 初始化结束后，初始化代理 IP 池
     *
     * @throws Exception e
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (ipPool == null || ipPool.isEmpty()) {
            initIpPool();
        }
    }
}
