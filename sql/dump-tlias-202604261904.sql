-- MySQL dump 10.13  Distrib 9.6.0, for macos14.8 (x86_64)
--
-- Host:     Database: tlias
-- ------------------------------------------------------
-- Server version	9.6.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '16880b34-1876-11f1-9627-2b70b125a5ce:1-222635';

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID，对应登录用户',
  `account_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账户号',
  `account_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '储蓄卡' COMMENT '账户类型',
  `balance` decimal(12,2) DEFAULT '0.00' COMMENT '账户余额',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '正常' COMMENT '账户状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='银行账户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,1,'6222000012345678','储蓄卡',90700.67,'正常','2026-01-11 17:20:48','2026-04-23 12:03:02'),(2,2,'6222000098765432','储蓄卡',11276.50,'正常','2026-01-11 17:25:12','2026-04-23 12:03:02'),(3,3,'6222000043210987','信用卡',9900.00,'正常','2026-01-11 17:30:05','2026-04-23 11:48:09'),(4,3,'6222000076543210','储蓄卡',35273.00,'正常','2026-01-11 17:35:20','2026-04-23 11:06:08');
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bank_product`
--

DROP TABLE IF EXISTS `bank_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bank_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '产品名称',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '产品类型（贷款/信用卡/理财）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '产品介绍',
  `interest_rate` decimal(5,2) DEFAULT NULL COMMENT '利率（可选）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='银行产品/业务说明表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bank_product`
--

LOCK TABLES `bank_product` WRITE;
/*!40000 ALTER TABLE `bank_product` DISABLE KEYS */;
/*!40000 ALTER TABLE `bank_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payee_contact`
--

DROP TABLE IF EXISTS `payee_contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payee_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `payee_name` varchar(64) NOT NULL COMMENT '收款人姓名',
  `account_no` varchar(32) NOT NULL COMMENT '收款人银行卡号',
  `alias` varchar(64) DEFAULT NULL COMMENT '备注/昵称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_account_no` (`user_id`,`account_no`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='常用收款人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payee_contact`
--

LOCK TABLES `payee_contact` WRITE;
/*!40000 ALTER TABLE `payee_contact` DISABLE KEYS */;
INSERT INTO `payee_contact` VALUES (4,1,'李季','6222000043210987','老李','2026-04-23 10:46:03','2026-04-23 10:46:03'),(6,1,'刘明','6222000098765432','弟弟','2026-04-23 10:46:56','2026-04-23 10:46:56');
/*!40000 ALTER TABLE `payee_contact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaction_record`
--

DROP TABLE IF EXISTS `transaction_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transaction_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` bigint NOT NULL,
  `counterparty_account_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `trade_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction_record`
--

LOCK TABLES `transaction_record` WRITE;
/*!40000 ALTER TABLE `transaction_record` DISABLE KEYS */;
INSERT INTO `transaction_record` VALUES (1,1,'6222000098765432','收入',5000.00,'工资','2026-01-11 18:47:17'),(2,1,'6222000076543210','支出',-200.00,'购物','2026-01-11 18:47:17'),(7,1,'6222000076543210','支出',-2000.00,'有钱！大把挥霍！','2026-01-11 19:31:54'),(8,4,'6222000012345678','收入',2000.00,'有钱！大把挥霍！','2026-01-11 19:31:54'),(9,1,'6222000076543210','支出',-111.00,'22222','2026-01-17 09:32:14'),(10,4,'6222000012345678','收入',111.00,'22222','2026-01-17 09:32:14'),(11,1,'6222000076543210','支出',-200.00,'111','2026-01-17 10:09:19'),(12,4,'6222000012345678','收入',200.00,'111','2026-01-17 10:09:19'),(13,1,'6222000076543210','支出',-123.00,'转账逻辑测试','2026-01-17 10:21:08'),(14,4,'6222000012345678','收入',123.00,'转账逻辑测试','2026-01-17 10:21:08'),(15,1,'6222000076543210','支出',-123.00,'转账逻辑测试','2026-01-17 10:21:41'),(16,4,'6222000012345678','收入',123.00,'转账逻辑测试','2026-01-17 10:21:41'),(17,1,'6222000076543210','支出',-123.00,'转账逻辑测试222','2026-01-17 10:21:49'),(18,4,'6222000012345678','收入',123.00,'转账逻辑测试222','2026-01-17 10:21:49'),(19,1,'6222000076543210','支出',-123.00,'逻辑测试第二次','2026-01-17 10:25:32'),(20,4,'6222000012345678','收入',123.00,'逻辑测试第二次','2026-01-17 10:25:32'),(21,1,'6222000076543210','支出',-60.00,'测试逻辑第3次','2026-01-17 10:27:54'),(22,4,'6222000012345678','收入',60.00,'测试逻辑第3次','2026-01-17 10:27:54'),(23,1,'6222000076543210','支出',-73.00,'','2026-03-08 22:43:36'),(24,4,'6222000012345678','收入',73.00,'','2026-03-08 22:43:36'),(25,1,'6222000076543210','支出',-300.00,'','2026-03-08 23:09:09'),(26,4,'6222000012345678','收入',300.00,'','2026-03-08 23:09:09'),(27,1,'6222000076543210','支出',-250.00,'','2026-03-09 00:02:27'),(28,4,'6222000012345678','收入',250.00,'','2026-03-09 00:02:27'),(29,1,'6222000076543210','支出',-300.00,'测试5566','2026-03-17 12:58:06'),(30,4,'6222000012345678','收入',300.00,'测试5566','2026-03-17 12:58:06'),(31,1,'6222000076543210','支出',-487.00,'测试3333','2026-03-17 14:19:40'),(32,4,'6222000012345678','收入',487.00,'测试3333','2026-03-17 14:19:40'),(33,1,'6222000043210987','支出',-10000.00,'风险测试','2026-03-22 16:14:21'),(34,3,'6222000012345678','收入',10000.00,'风险测试','2026-03-22 16:14:21'),(35,3,'6222000012345678','支出',-10000.00,'大额支出','2026-04-07 14:37:33'),(36,1,'6222000043210987','收入',10000.00,'大额支出','2026-04-07 14:37:33'),(37,3,'6222000012345678','支出',-100.00,'小额','2026-04-07 14:37:42'),(38,1,'6222000043210987','收入',100.00,'小额','2026-04-07 14:37:42'),(39,3,'6222000012345678','支出',-1000.00,'小额','2026-04-07 14:37:52'),(40,1,'6222000043210987','收入',1000.00,'小额','2026-04-07 14:37:52'),(41,1,'6222000098765432','支出',-300.00,'弟弟','2026-04-23 10:55:17'),(42,2,'6222000012345678','收入',300.00,'弟弟','2026-04-23 10:55:17'),(43,1,'6222000098765432','支出',-300.00,'弟弟的午餐费','2026-04-21 10:55:38'),(44,2,'6222000012345678','收入',300.00,'弟弟的午餐费','2026-04-21 10:55:38'),(45,1,'6222000098765432','支出',-100.00,'晚餐费','2026-04-17 11:04:47'),(46,2,'6222000012345678','收入',100.00,'晚餐费','2026-04-17 11:04:47'),(47,1,'6222000043210987','支出',-3000.00,'团建费','2026-03-23 11:05:44'),(48,3,'6222000012345678','收入',3000.00,'团建费','2026-03-23 11:05:44'),(49,1,'6222000076543210','支出',-3000.00,'','2026-04-23 11:06:08'),(50,4,'6222000012345678','收入',3000.00,'','2026-04-23 11:06:08'),(51,1,'6222000098765432','支出',-100.00,'晚餐费','2026-04-23 11:22:44'),(52,2,'6222000012345678','收入',100.00,'晚餐费','2026-04-23 11:22:44'),(53,1,'6222000098765432','支出',-300.00,'午餐费','2026-04-23 11:44:56'),(54,2,'6222000012345678','收入',300.00,'午餐费','2026-04-23 11:44:56'),(55,1,'6222000098765432','支出',-100.00,'晚餐费','2026-04-23 11:45:23'),(56,2,'6222000012345678','收入',100.00,'晚餐费','2026-04-23 11:45:23'),(57,1,'6222000098765432','支出',-100.00,'晚餐费','2026-04-23 11:46:52'),(58,2,'6222000012345678','收入',100.00,'晚餐费','2026-04-23 11:46:52'),(59,1,'6222000043210987','支出',-3000.00,'团建费','2026-04-23 11:48:10'),(60,3,'6222000012345678','收入',3000.00,'团建费','2026-04-23 11:48:10'),(61,1,'6222000098765432','支出',-100.00,'晚餐费','2026-04-23 12:03:03'),(62,2,'6222000012345678','收入',100.00,'晚餐费','2026-04-23 12:03:03');
/*!40000 ALTER TABLE `transaction_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '123456' COMMENT '密码',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `username` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='员工表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'DuQnng','wq123456','2026-01-11 02:53:18','2026-01-11 02:53:18','15114949450','王奇'),(2,'zhangwuji','123456','2026-01-04 18:16:39','2026-01-04 18:16:39','13992277779','刘明'),(3,'yangxiao','123456','2026-01-04 18:16:39','2026-01-04 18:16:39','13992277780','李季');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'tlias'
--
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-26 19:04:42
