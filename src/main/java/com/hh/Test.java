package com.hh;

import com.hh.function.ipproxy.IpProxy;
import com.hh.function.ipproxy.XiaoxiangIpProxy;
import com.hh.utils.DataBaseUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;

public class Test {
    public static ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
    public static DataBaseUtils dataBaseUtils = context.getBean("dataBaseUtils", DataBaseUtils.class);
    public static IpProxy ipProxy = context.getBean("xiaoxiangIpProxy", XiaoxiangIpProxy.class);

    public static void main(String[] args1) {
        String url = "https://blog.csdn.net/shenshaoming/article/details/115752215";
        String[] split = url.split("/");
        String hostName = split[2];
        int port = 80;
        if ("https:".equals(split[0])){
            port = 443;
        }
        // 如果 url 中指定了端口，则使用指定端口 并 去掉 hostName 中的 “:”
        if (hostName.contains(":")) {
            String[] args = hostName.split(":");
            hostName = args[0];
            port = Integer.parseInt(args[1]);
        }
        System.out.println(hostName);
        System.out.println(port);

    }


}

