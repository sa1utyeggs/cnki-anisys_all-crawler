package com.hh.function.ipproxy;

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

    /**
     * 随机获取 IP
     *
     * @return ip
     */
    public ProxyIp getIpRandomly() {
        if (!isEmpty()) {
            int ri = random.nextInt(pool.size());
            return getIp(ri);
        }
        return null;
    }

    /**
     * 获取指定下标的对象，使用 % 操作保证不会超限
     *
     * @param i i
     * @return ip
     */
    public ProxyIp getIp(int i) {
        if (i >= 0 && !isEmpty()) {
            while (!isEmpty()) {
                // 防止超限
                i %= pool.size();
                ProxyIp proxyIp = pool.get(i);
                if (proxyIp.isExpired()) {
                    pool.remove(i);
                } else {
                    return proxyIp;
                }
            }
        }
        return null;
    }


    public void removeIp(ProxyIp ip) {
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
