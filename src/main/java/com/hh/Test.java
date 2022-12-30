package com.hh;

import com.hh.utils.DataBaseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {
    private static final Logger logger = LogManager.getLogger(Test.class);
    public static ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
    public static DataBaseUtils dataBaseUtils = context.getBean("dataBaseUtils", DataBaseUtils.class);

    public static void main(String[] args1) {
       Main.searchAndInsert("鼻窦炎", true, true);

    }


}

