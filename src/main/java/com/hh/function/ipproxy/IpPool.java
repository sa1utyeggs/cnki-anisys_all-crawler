package com.hh.function.ipproxy;

import com.hh.utils.AssertUtils;
import lombok.Data;

import java.util.List;
import java.util.Random;

/**
 * @author 86183
 */
@Data
public class IpPool {
    private List<ProxyIp> pool;
    private Random random;

    public IpPool(List<ProxyIp> pool) {
        this.pool = pool;
        random = new Random();
    }

    public ProxyIp getIpRandomly() {
        AssertUtils.sysIsError(isEmpty(),"代理 IP 池为 空，无法进行代理操作");
        int ri = random.nextInt(pool.size());
        ProxyIp proxyIp = pool.get(ri);
        if (proxyIp.isExpired()) {
            // 如果该代理过期，则删除该 ip，并重新获取 IP；
            pool.remove(ri);
            return getIpRandomly();
        } else {
            return proxyIp;
        }
    }

    public void removeInvalidIp(ProxyIp ip) {
        for (int i = 0; i < pool.size(); i++) {
            if (pool.get(i).equals(ip)) {
                pool.remove(i);
                return;
            }
        }
    }

    public boolean isEmpty() {
        return pool == null || pool.isEmpty();
    }
}
