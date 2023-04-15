# 介绍
知网特定信息爬虫（实现部分不能爬取非登录用户无法访问的数据）

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
当前版本引入 Spring 容器管理；

考虑到敏感数据（数据库连接参数、代理 IP API 参数）的问题，使用 resources/spring/application-sensitive.xml文件存储敏感数据。


#备注
正在重构本项目，可能会出现各种bug；