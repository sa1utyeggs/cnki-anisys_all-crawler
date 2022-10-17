/*
 Navicat Premium Data Transfer

 Source Server         : hh_rds_aliyun-diet_disease_dba
 Source Server Type    : MySQL
 Source Server Version : 50735
 Source Host           : rm-bp1i2mlyhe9zc39cgho.mysql.rds.aliyuncs.com:3306
 Source Schema         : diet_disease

 Target Server Type    : MySQL
 Target Server Version : 50735
 File Encoding         : 65001

 Date: 17/10/2022 16:19:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for diet
-- ----------------------------
DROP TABLE IF EXISTS `diet`;
CREATE TABLE `diet`  (
  `name` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `level` tinyint(255) NULL DEFAULT 0,
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 444 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for diet_metabolite
-- ----------------------------
DROP TABLE IF EXISTS `diet_metabolite`;
CREATE TABLE `diet_metabolite`  (
  `dietName` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `metaboliteName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `content` float NULL DEFAULT NULL,
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `metaboliteName`(`metaboliteName`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for diet_metabolite_alias
-- ----------------------------
DROP TABLE IF EXISTS `diet_metabolite_alias`;
CREATE TABLE `diet_metabolite_alias`  (
  `name` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '饮食名',
  `alias` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '别称',
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `priority` int(10) UNSIGNED NULL DEFAULT 99 COMMENT '优先级（越低越优先），影响paper_main_sentence中，若出现了高优先级的关键词，则不会出现低优先级的句子；',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1072 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for diet_relationship
-- ----------------------------
DROP TABLE IF EXISTS `diet_relationship`;
CREATE TABLE `diet_relationship`  (
  `parentDiet` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `sonDiet` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dietRelationship_ibfk_2`(`sonDiet`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 428 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for diet_translation
-- ----------------------------
DROP TABLE IF EXISTS `diet_translation`;
CREATE TABLE `diet_translation`  (
  `chineseName` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `englishName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for disease
-- ----------------------------
DROP TABLE IF EXISTS `disease`;
CREATE TABLE `disease`  (
  `name` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `level` tinyint(255) NULL DEFAULT 0,
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 503 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for disease_alias
-- ----------------------------
DROP TABLE IF EXISTS `disease_alias`;
CREATE TABLE `disease_alias`  (
  `name` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `alias` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '别名',
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `priority` int(11) NULL DEFAULT 99 COMMENT '优先级（越低越优先），影响paper_main_sentence中，若出现了高优先级的关键词，则不会出现低优先级的句子；',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for disease_relationship
-- ----------------------------
DROP TABLE IF EXISTS `disease_relationship`;
CREATE TABLE `disease_relationship`  (
  `parentDisease` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `sonDisease` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `diseaseRelationship_ibfk_2`(`sonDisease`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 517 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for disease_translation
-- ----------------------------
DROP TABLE IF EXISTS `disease_translation`;
CREATE TABLE `disease_translation`  (
  `chineseName` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `englishName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for metabolite
-- ----------------------------
DROP TABLE IF EXISTS `metabolite`;
CREATE TABLE `metabolite`  (
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_name_idx`(`name`) USING BTREE COMMENT '代谢物名唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1028 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for metabolite_disease_number
-- ----------------------------
DROP TABLE IF EXISTS `metabolite_disease_number`;
CREATE TABLE `metabolite_disease_number`  (
  `metabolite` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `disease` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `number` int(11) NULL DEFAULT NULL COMMENT '论文数量',
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `checked` tinyint(4) NULL DEFAULT 0 COMMENT '是否完成数据收集',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_disease_number`(`disease`, `number`) USING BTREE,
  INDEX `idx_disease_metabolite`(`disease`, `metabolite`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1541 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for paper_abstract
-- ----------------------------
DROP TABLE IF EXISTS `paper_abstract`;
CREATE TABLE `paper_abstract`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `paper_id` bigint(20) UNSIGNED NULL DEFAULT NULL COMMENT '文章的主键',
  `text` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '文章概要',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15535 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for paper_info
-- ----------------------------
DROP TABLE IF EXISTS `paper_info`;
CREATE TABLE `paper_info`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `metabolite` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '代谢物',
  `disease` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '疾病',
  `title` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '题目',
  `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '访问url',
  `relation` tinyint(1) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '代谢物与疾病的关系（0：默认，1：不相关，2：正相关，3负相关）',
  `confidence` float(6, 5) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '置信度',
  `main_sentence_id` bigint(20) NULL DEFAULT NULL COMMENT '使用了paper_main_sentence中的那一句话得到的结果',
  `source` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据来源',
  `unique_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '作为索引查找的唯一值',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_unique_key`(`unique_key`) USING BTREE,
  INDEX `idx_disease`(`disease`) USING BTREE,
  INDEX `idx_metabolite`(`metabolite`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15535 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for paper_main_sentence
-- ----------------------------
DROP TABLE IF EXISTS `paper_main_sentence`;
CREATE TABLE `paper_main_sentence`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `paper_id` bigint(20) UNSIGNED NULL DEFAULT NULL COMMENT '文章的主键',
  `text` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '句子内容',
  `head` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'nlp的第一个关键词',
  `tail` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'nlp的第二个关键词',
  `head_offset` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '第一个关键词，在句子中的位置',
  `tail_offset` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '第二个关键词，在句子中的位置',
  `relation` tinyint(1) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '预测关系',
  `confidence` float UNSIGNED NULL DEFAULT NULL COMMENT '置信度',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_paper_id`(`paper_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32132 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for relationship
-- ----------------------------
DROP TABLE IF EXISTS `relationship`;
CREATE TABLE `relationship`  (
  `reference` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `dietName` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `diseaseName` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `correlation` tinyint(255) NULL DEFAULT NULL,
  `confidence` float NULL DEFAULT NULL,
  `contributor` char(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `relationship_ibfk_1`(`dietName`) USING BTREE,
  INDEX `relationship_ibfk_2`(`diseaseName`) USING BTREE,
  INDEX `contributor`(`contributor`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for text
-- ----------------------------
DROP TABLE IF EXISTS `text`;
CREATE TABLE `text`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
  `abstract` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '文章简要',
  `main_sentence` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '主要句子',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `userID` char(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` char(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Function structure for get_child_list
-- ----------------------------
DROP FUNCTION IF EXISTS `get_child_list`;
delimiter ;;
CREATE FUNCTION `get_child_list`(in_id varchar(10))
 RETURNS varchar(1000) CHARSET utf8
begin 
 declare ids varchar(1000) default ''; 
 declare tempids varchar(1000); 
 
 set tempids = in_id; 
 while tempids is not null do 
  set ids = CONCAT_WS(',',ids,tempids); 
  select GROUP_CONCAT(id) into tempids from dept where FIND_IN_SET(pid,tempids)>0;  
 end while; 
 return ids; 
end
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
