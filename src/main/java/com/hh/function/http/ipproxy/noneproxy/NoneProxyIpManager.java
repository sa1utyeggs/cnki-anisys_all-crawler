package com.hh.function.http.ipproxy.noneproxy;

import com.hh.function.base.ThreadPoolFactory;
import com.hh.function.http.ipproxy.AsyncProxyIpManager;
import com.hh.function.http.ipproxy.ProxyIp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 86183
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NoneProxyIpManager extends NoneAbstractProxyIpManager implements AsyncProxyIpManager {


    public NoneProxyIpManager() {
        super();
        this.logger = LogManager.getLogger(NoneProxyIpManager.class);
    }

    /**
     * 不返回代理 IP
     *
     * @return ip
     */
    @Override
    public ProxyIp getIp() {
        return doGetIp();
    }

    /**
     * 不返回代理 IP
     *
     * @return ip null
     */
    private ProxyIp doGetIp() {
        return null;
    }

    /**
     * 打印日志
     */
    @Override
    public void afterPropertiesSet() {
        initIpPool();
    }
}
