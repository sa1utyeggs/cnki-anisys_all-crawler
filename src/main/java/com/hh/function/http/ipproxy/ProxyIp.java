package com.hh.function.http.ipproxy;


import com.hh.function.base.Const;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Objects;

/**
 * @author 86183
 */
@Data
@Builder
public class ProxyIp {
    private String ip;
    private Integer port;
    private boolean expirable;
    private Date expireTime;



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProxyIp proxyIp = (ProxyIp) o;
        return ip.equals(proxyIp.ip);
    }

    /**
     * 判断当前 IP 是否过期
     * @return boolean
     */
    public boolean isExpired(){
        return expirable && expireTime.getTime() <= (System.currentTimeMillis() + Const.IP_TIME * 1000);
    }

    /**
     * 判断 IP 是否可用
     * @param ip ip
     * @return boolean
     */
    public static boolean isValid(ProxyIp ip){
        return !(ip == null || ip.isExpired());
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip);
    }
}
