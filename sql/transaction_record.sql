/*
 Navicat Premium Dump SQL

 Source Server         : DuQnng
 Source Server Type    : MySQL
 Source Server Version : 90200 (9.2.0)
 Source Host           : localhost:3306
 Source Schema         : tlias

 Target Server Type    : MySQL
 Target Server Version : 90200 (9.2.0)
 File Encoding         : 65001

 Date: 22/03/2026 16:49:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for transaction_record
-- ----------------------------
DROP TABLE IF EXISTS `transaction_record`;
CREATE TABLE `transaction_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` bigint NOT NULL,
  `counterparty_account_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `amount` decimal(12, 2) NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `trade_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of transaction_record
-- ----------------------------
INSERT INTO `transaction_record` VALUES (1, 1, '6222000098765432', '收入', 5000.00, '工资', '2026-01-11 18:47:17');
INSERT INTO `transaction_record` VALUES (2, 1, '6222000076543210', '支出', -200.00, '购物', '2026-01-11 18:47:17');
INSERT INTO `transaction_record` VALUES (7, 1, '6222000076543210', '支出', -2000.00, '有钱！大把挥霍！', '2026-01-11 19:31:54');
INSERT INTO `transaction_record` VALUES (8, 4, '6222000012345678', '收入', 2000.00, '有钱！大把挥霍！', '2026-01-11 19:31:54');
INSERT INTO `transaction_record` VALUES (9, 1, '6222000076543210', '支出', -111.00, '22222', '2026-01-17 09:32:14');
INSERT INTO `transaction_record` VALUES (10, 4, '6222000012345678', '收入', 111.00, '22222', '2026-01-17 09:32:14');
INSERT INTO `transaction_record` VALUES (11, 1, '6222000076543210', '支出', -200.00, '111', '2026-01-17 10:09:19');
INSERT INTO `transaction_record` VALUES (12, 4, '6222000012345678', '收入', 200.00, '111', '2026-01-17 10:09:19');
INSERT INTO `transaction_record` VALUES (13, 1, '6222000076543210', '支出', -123.00, '转账逻辑测试', '2026-01-17 10:21:08');
INSERT INTO `transaction_record` VALUES (14, 4, '6222000012345678', '收入', 123.00, '转账逻辑测试', '2026-01-17 10:21:08');
INSERT INTO `transaction_record` VALUES (15, 1, '6222000076543210', '支出', -123.00, '转账逻辑测试', '2026-01-17 10:21:41');
INSERT INTO `transaction_record` VALUES (16, 4, '6222000012345678', '收入', 123.00, '转账逻辑测试', '2026-01-17 10:21:41');
INSERT INTO `transaction_record` VALUES (17, 1, '6222000076543210', '支出', -123.00, '转账逻辑测试222', '2026-01-17 10:21:49');
INSERT INTO `transaction_record` VALUES (18, 4, '6222000012345678', '收入', 123.00, '转账逻辑测试222', '2026-01-17 10:21:49');
INSERT INTO `transaction_record` VALUES (19, 1, '6222000076543210', '支出', -123.00, '逻辑测试第二次', '2026-01-17 10:25:32');
INSERT INTO `transaction_record` VALUES (20, 4, '6222000012345678', '收入', 123.00, '逻辑测试第二次', '2026-01-17 10:25:32');
INSERT INTO `transaction_record` VALUES (21, 1, '6222000076543210', '支出', -60.00, '测试逻辑第3次', '2026-01-17 10:27:54');
INSERT INTO `transaction_record` VALUES (22, 4, '6222000012345678', '收入', 60.00, '测试逻辑第3次', '2026-01-17 10:27:54');
INSERT INTO `transaction_record` VALUES (23, 1, '6222000076543210', '支出', -73.00, '', '2026-03-08 22:43:36');
INSERT INTO `transaction_record` VALUES (24, 4, '6222000012345678', '收入', 73.00, '', '2026-03-08 22:43:36');
INSERT INTO `transaction_record` VALUES (25, 1, '6222000076543210', '支出', -300.00, '', '2026-03-08 23:09:09');
INSERT INTO `transaction_record` VALUES (26, 4, '6222000012345678', '收入', 300.00, '', '2026-03-08 23:09:09');
INSERT INTO `transaction_record` VALUES (27, 1, '6222000076543210', '支出', -250.00, '', '2026-03-09 00:02:27');
INSERT INTO `transaction_record` VALUES (28, 4, '6222000012345678', '收入', 250.00, '', '2026-03-09 00:02:27');
INSERT INTO `transaction_record` VALUES (29, 1, '6222000076543210', '支出', -300.00, '测试5566', '2026-03-17 12:58:06');
INSERT INTO `transaction_record` VALUES (30, 4, '6222000012345678', '收入', 300.00, '测试5566', '2026-03-17 12:58:06');
INSERT INTO `transaction_record` VALUES (31, 1, '6222000076543210', '支出', -487.00, '测试3333', '2026-03-17 14:19:40');
INSERT INTO `transaction_record` VALUES (32, 4, '6222000012345678', '收入', 487.00, '测试3333', '2026-03-17 14:19:40');
INSERT INTO `transaction_record` VALUES (33, 1, '6222000043210987', '支出', -10000.00, '风险测试', '2026-03-22 16:14:21');
INSERT INTO `transaction_record` VALUES (34, 3, '6222000012345678', '收入', 10000.00, '风险测试', '2026-03-22 16:14:21');

SET FOREIGN_KEY_CHECKS = 1;
