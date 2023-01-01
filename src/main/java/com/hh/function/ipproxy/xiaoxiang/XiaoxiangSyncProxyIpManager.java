package com.hh.function.ipproxy.xiaoxiang;

import com.hh.function.ipproxy.ProxyIp;
import com.hh.function.ipproxy.SyncProxyIpManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author 86183
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Deprecated
public class XiaoxiangSyncProxyIpManager extends XiaoxiangAbstractProxyIpManager implements SyncProxyIpManager {

    public XiaoxiangSyncProxyIpManager() {
        super();
        logger = LogManager.getLogger(XiaoxiangSyncProxyIpManager.class);
    }

    @Override
    public ProxyIp getIp() {
        ProxyIp proxyIp = ipPool.getIpRandomly();
        while (proxyIp == null) {
            logger.warn("获取 IP 失败，尝试重新初始化代理 IP 池");
            try {
                initIpPool();
            } catch (Exception e) {
                e.printStackTrace();
            }
            proxyIp = ipPool.getIpRandomly();
        }
        return proxyIp;
    }
}
