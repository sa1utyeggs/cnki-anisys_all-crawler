package com.hh.function.base;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author 86183
 * 用于获得 ApplicationContext 对象
 */
public class ContextSingletonFactory {
    private static volatile ApplicationContext context;
    private static final Object MUTEX = new Object();

    public static ApplicationContext getInstance() {
        if (context == null) {
            synchronized (MUTEX) {
                if (context == null) {
                    context = new ClassPathXmlApplicationContext("applicationContext.xml");
                }
            }
        }
        return context;
    }
}
