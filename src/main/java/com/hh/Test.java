package com.hh;

import cn.hutool.crypto.digest.DigestUtil;
import com.hh.entity.MainSentence;
import com.hh.function.Const;
import com.hh.utils.DataBaseUtils;
import com.hh.utils.FileUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class Test {
    public static ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
    public static DataBaseUtils dataBaseUtils = context.getBean("dataBaseUtils", DataBaseUtils.class);

    public static void main(String[] args) {
        String source = Const.SOURCE_ZHIWANG;
        // 根据 url 以及 source 生成 唯一值
        String uniqueKey = DigestUtil.md5Hex("https://kns.cnki.net/kcms/detail/detail.aspx?FileName=TDYX202106020&DbName=CJFDLAST2021&DbCode=CJFD&yx=&pr=&URLID=" + source);
        System.out.println(uniqueKey);
    }


}

