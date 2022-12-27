package com.hh.function.proxy;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hh.utils.DateUtils;
import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author 86183
 */
@Data
public class XiaoxiangIpProxy implements IpProxy, InitializingBean {
    private String url;
    private String appKey;
    private String appSecret;
    private String cnt;
    private IpPool ipPool;

    @Override
    public ProxyIp getIp() {
        try {
            return ipPool.getIpRandomly();
        } catch (Exception e1) {
            System.out.println(e1.getMessage() + "，尝试重新初始化代理 IP 池");
            try {
                initIpPool();
                return ipPool.getIpRandomly();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void initIpPool() throws IOException {
        Connection con;
        try {
            con = Jsoup.connect(url);
            con.header("Accept", "text/html, */*; q=0.01");
            con.header("Content-Type", "application/json; charset=UTF-8");
            con.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36");
            con.header("Host", "api.xiaoxiangdaili.com");

            // 忽略返回数据类型
            con.ignoreContentType(true);

            // 设置请求参数
            con.data("appKey", appKey);
            con.data("appSecret", appSecret);
            con.data("cnt", String.valueOf(cnt));
            con.data("wt", "json");
            con.data("method", "http");

            // 获取 JSON，并加工 IP 池
            JSONObject jsonObject = JSONObject.parseObject(con.get().body().text());
            JSONArray data = jsonObject.getJSONArray("data");
            ArrayList<ProxyIp> proxyIps = new ArrayList<>();
            for (Object datum : data) {
                JSONObject o = (JSONObject) datum;
                proxyIps.add(ProxyIp.builder()
                        .ip(o.getString("ip"))
                        .port(o.getInteger("port"))
                        .expirable(true)
                        .expireTime(DateUtils.getDateAfter(o.getDate("startTime"), o.getInteger("during") * 60))
                        .build());
            }

            ipPool = new IpPool(proxyIps);
        } catch (Exception e) {
            System.out.println("初始化小象代理 IP 池失败");
            throw e;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("after Init");
        if (ipPool == null || ipPool.isEmpty()) {
            initIpPool();
        }
    }
}
