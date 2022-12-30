package com.hh.function.ipproxy;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hh.utils.DateUtils;
import com.hh.utils.HttpConnectionPoolUtil;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author 86183
 * 单线程情况下的 IP管理
 */
@Data
public class XiaoxiangProxyIpManager implements ProxyIpManager, InitializingBean {
    private String url;
    private String appKey;
    private String appSecret;
    private String cnt;
    protected IpPool ipPool;
    private Date lastUpdate;
    protected final Logger logger = LogManager.getLogger(XiaoxiangProxyIpManager.class);

    public static final int XIAOXIANG_ACCESS_INTERVAL = 10 + 1;

    public XiaoxiangProxyIpManager() {
        // 初始化值为前 1 天的 0 点；
        lastUpdate = DateUtils.getDaysBeforeToday(1);
    }

    @Override
    public ProxyIp getIp() {
        ProxyIp proxyIp = ipPool.getIpRandomly();
        while (proxyIp == null) {
            logger.warn("无可用 IP，尝试重新初始化代理 IP 池");
            try {
                initIpPool();
            } catch (Exception e) {
                e.printStackTrace();
            }
            proxyIp = ipPool.getIpRandomly();
        }
        return proxyIp;
    }

    @Override
    public void initIpPool() throws Exception {
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

            // 由于小象 ip 代理池有访问间隔的限制，若上次的访问时间里本次过近则会返回 null；
            // 需要等待，防止永远无法获得代理 IP
            int differ = DateUtils.differForSeconds(lastUpdate, new Date());
            if (differ < XIAOXIANG_ACCESS_INTERVAL) {
                int pause = Math.min(XIAOXIANG_ACCESS_INTERVAL, Math.abs(XIAOXIANG_ACCESS_INTERVAL - differ));
                logger.warn("访问小象代理过于频繁，暂停" + pause + "s");
                Thread.sleep(pause * 1000L);
            }

            // 获取 JSON，并加工 IP 池
            Document document = con.get();
            lastUpdate = new Date();
            JSONObject jsonObject = JSONObject.parseObject(document.body().text());
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
            logger.info("初始化代理 IP 池成功");
        } catch (Exception e) {
            logger.error("初始化小象代理 IP 池失败");
            throw e;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (ipPool == null || ipPool.isEmpty()) {
            initIpPool();
        }
    }
}
