package com.hh.function.system;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author 86183
 */
public class ContextSingltonFactory {
    private static volatile ApplicationContext context;
    private static final Object mutex = new Object();

    public static ApplicationContext getInstance() {
        if (context == null) {
            synchronized (mutex) {
                if (context == null) {
                    context = new ClassPathXmlApplicationContext("applicationContext.xml");
                }
            }
        }
        return context;
    }
}
