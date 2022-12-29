# cnki-anisys_all-crawler
知网爬虫
使用的时候，将DataBaseUtils中的数据库改成自己用的；
或者将数据库相关代码注释掉；

已经提供了底层的爬取逻辑，各位可以使用 function 包中的工具自行编辑爬取代码；

如果爬取不成功，可以修改cookie试试  

当前版本引入 Spring 容器管理，需要在 resource 中加入一个 applicationContext.xml 文件；

考虑到敏感数据问题，这里提供 applicationContext.xml 文件的模板：

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    https://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.springframework.org/schema/aop
    https://www.springframework.org/schema/aop/spring-aop.xsd">
    
        <bean id="dataBaseUtils" class="com.hh.utils.DataBaseUtils">
            <property name="databaseUrl"
                      value="数据库URL"/>
            <property name="databaseUsername"
                      value="账户"/>
            <property name="databasePassword"
                      value="密码"/>
        </bean>
    
        <bean id="xiaoxiangIpProxy" class="com.hh.function.ipproxy.XiaoxiangIpProxy">
            <property name="url" value="代理IP 提供商 URL"/>
            <property name="appKey" value="应用 KEY"/>
            <property name="appSecret" value="应用密码"/>
            <property name="cnt" value="每次获得的 IP 数量"/>
        </bean>
    
        <bean id="connectionFactory" class="com.hh.function.system.ConnectionFactory">
            <!-- ipProxy 可以根据 对应代理 IP 提供商 自定义一个 IpProxy 类 -->
            <property name="ipProxy" ref="xiaoxiangIpProxy"/>
        </bean>
    
    
        <bean id="shellBanner" class="com.hh.aop.ShellBanner"/>
    
        <!-- 第三种：使用注解 -->
        <aop:aspectj-autoproxy/>
    </beans>

## 功能
当前版本使用 Spring容器管理数据库连接，可实现连接复用，增加效率；   
PaperDetail：爬取论文详情页面；
当然你也可以自己使用其余的方法来编写自己的爬虫；  
insertPaperInfo方法是这个类的入口，对于搜索的参数，可以自己指定，不一定是饮食与疾病；  

PaperNum：爬取论文数量：  
getMetabolitesDiseasePaperNum方法是入口，其余同上；  

###数据库表结构
doc/diet_disease.sql
