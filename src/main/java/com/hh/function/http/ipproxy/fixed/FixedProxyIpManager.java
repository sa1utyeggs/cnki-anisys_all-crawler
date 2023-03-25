package com.hh.function.http.ipproxy.fixed;

import com.hh.function.http.ipproxy.AsyncProxyIpManager;
import com.hh.function.http.ipproxy.ProxyIp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.logging.log4j.LogManager;

/**
 * @author 86183
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FixedProxyIpManager extends FixedAbstractProxyIpManager implements AsyncProxyIpManager {


    public FixedProxyIpManager() {
        super();
        this.logger = LogManager.getLogger(FixedProxyIpManager.class);
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
        return getProxyIp();
    }

}
