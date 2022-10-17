# cnki-anisys_all-crawler
知网爬虫
使用的时候，将DataBaseUtils中的数据库改成自己用的；
或者将数据库相关代码注释掉；

已经提供了底层的爬取逻辑，各位可以使用 function 包中的工具自行编辑爬取代码；

如果爬取不成功，可以修改cookie试试  

## 功能
当前版本使用 Spring容器管理数据库连接，可实现连接复用，增加效率；   
PaperDetail：爬取论文详情页面；
当然你也可以自己使用其余的方法来编写自己的爬虫；  
insertPaperInfo方法是这个类的入口，对于搜索的参数，可以自己指定，不一定是饮食与疾病；  

PaperNum：爬取论文数量：  
getMetabolitesDiseasePaperNum方法是入口，其余同上；  

###数据库表结构
doc/diet_disease.sql
