package com.hh;

import com.hh.function.proxy.IpPool;
import com.hh.function.proxy.IpProxy;
import com.hh.function.proxy.XiaoxiangIpProxy;
import com.hh.utils.DataBaseUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class Test {
    public static ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
    public static DataBaseUtils dataBaseUtils = context.getBean("dataBaseUtils", DataBaseUtils.class);
    public static IpProxy ipProxy = context.getBean("xiaoxiangIpProxy", XiaoxiangIpProxy.class);

    public static void main(String[] args) throws IOException {
        // ipProxy.initIpPool();
        System.out.println(ipProxy.getIp());
    }


}

