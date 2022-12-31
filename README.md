# 介绍
知网特定信息爬虫（不能爬取非登录用户无法访问的数据）

使用时，可以将 DataBaseUtils 中的数据库改成自己用的，或者使用 test = true 来避免使用数据库逻辑；

已经提供了底层的爬取逻辑，各位可以使用 function.system 包中的类和 utils 包中的工具类，自行编辑爬取代码；

~~如果爬取不成功，可以修改 cookie 试试~~。对于本项目，cookie 并不是必要的；若有使用 cookie 的必要可以修改 resources/cookie.txt



# 功能
当前版本使用 Spring容器管理数据库连接，可实现连接复用，增加效率；   
PaperDetail：爬取论文详情页面；
当然你也可以自己使用其余的方法来编写自己的爬虫；  
insertPaperInfo方法是这个类的入口，对于搜索的参数，可以自己指定，不一定是饮食与疾病；

PaperNum：爬取论文数量：  
getMetabolitesDiseasePaperNum() 方法是入口，其余同上；

##数据库表结构
doc/diet_disease.sql

# applicationContext.xml
当前版本引入 Spring 容器管理；需要在 resource 中加入一个 applicationContext.xml 文件；



考虑到敏感数据（数据库连接参数、代理 IP API 参数）的问题，这里提供 applicationContext.xml 文件的模板：

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    https://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.springframework.org/schema/aop
    https://www.springframework.org/schema/aop/spring-aop.xsd">
    
        <bean id="dataSource" class="com.hh.function.system.DataSource">
            <property name="databaseUrl"
                      value="<MySQL-URL>"/>
            <property name="databaseUsername"
                      value="<username>"/>
            <property name="databasePassword"
                      value="<password>"/>
        </bean>
    
        <bean id="dataBaseUtils" class="com.hh.utils.DataBaseUtils">
            <property name="dataSource" ref="dataSource"/>
        </bean>
    
        <bean id="xiaoxiangProxyIpManager" class="com.hh.function.ipproxy.XiaoxiangProxyIpManager">
            <property name="url" value="<小象代理 IP 获取 url>"/>
            <property name="appKey" value="<appKey>"/>
            <property name="appSecret" value="<appSecret>"/>
            <property name="cnt" value="<每次获得 IP 的个数>"/>
        </bean>
    
        <bean id="threadPoolFactory" class="com.hh.function.system.ThreadPoolFactory">
            <!-- 所有线程池的线程数量统一 -->
            <property name="threadNum" value="1"/>
        </bean>
    
    
        <bean id="shellBanner" class="com.hh.aop.ShellBanner"/>
    
        <!-- 第三种：使用注解 -->
        <aop:aspectj-autoproxy/>
    </beans>

