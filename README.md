# cnki-anisys_all-crawler
知网爬虫
使用的时候，将DataBaseUtils中的数据库改成自己用的；
或者将数据库相关代码注释掉；

如果爬取不成功，可以修改cookie试试  

## 功能
PaperDetail：爬取论文详情页面：  
当然你也可以自己使用其余的方法来编写自己的爬虫；  
insertPaperInfo方法是这个类的入口，对于搜索的参数，可以自己指定，不一定是饮食与疾病；  

PaperNum：爬取论文数量：  
getMetabolitesDiseasePaperNum方法是入口，其余同上；  

###insertPaperInfo方法的表结构
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for paper_info
-- ----------------------------
DROP TABLE IF EXISTS `paper_info`;
CREATE TABLE `paper_info`  (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`metabolite` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '代谢物',
`disease` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '疾病',
`title` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '题目',
`url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '访问url',
`abstractText` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '摘要',
`mainSentence` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '主要判断的句子',
PRIMARY KEY (`id`) USING BTREE,
INDEX `disease_idx`(`disease`) USING BTREE COMMENT '疾病名的索引',
INDEX `metabolite_idx`(`metabolite`) USING BTREE COMMENT '代谢物的索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1426 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
