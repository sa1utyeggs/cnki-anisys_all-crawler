package com.hh;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.hh.function.application.CnkiDatabaseService;
import com.hh.function.base.ContextSingletonFactory;
import com.hh.function.base.DataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {
    public static ApplicationContext context = ContextSingletonFactory.getInstance();
    public static CnkiDatabaseService dataBaseUtils = context.getBean("dataBaseUtils", CnkiDatabaseService.class);

    public static void main(String[] args1) {
       Main.searchAndInsert("鼻窦炎", false, true);

    }


}

