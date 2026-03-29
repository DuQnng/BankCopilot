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

 Date: 22/03/2026 16:48:53
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID，对应登录用户',
  `account_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账户号',
  `account_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '储蓄卡' COMMENT '账户类型',
  `balance` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '账户余额',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '正常' COMMENT '账户状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '银行账户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of account
-- ----------------------------
INSERT INTO `account` VALUES (1, 1, '6222000012345678', '储蓄卡', 90000.67, '正常', '2026-01-11 17:20:48', '2026-03-22 16:14:20');
INSERT INTO `account` VALUES (2, 2, '6222000098765432', '储蓄卡', 9876.50, '正常', '2026-01-11 17:25:12', '2026-01-11 17:25:12');
INSERT INTO `account` VALUES (3, 3, '6222000043210987', '信用卡', 15000.00, '正常', '2026-01-11 17:30:05', '2026-03-22 16:14:20');
INSERT INTO `account` VALUES (4, 4, '6222000076543210', '储蓄卡', 32273.00, '正常', '2026-01-11 17:35:20', '2026-03-17 14:19:40');

SET FOREIGN_KEY_CHECKS = 1;
