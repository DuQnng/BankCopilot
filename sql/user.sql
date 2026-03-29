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

 Date: 22/03/2026 16:49:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '123456' COMMENT '密码',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'DuQnng', 'wq123456', '2026-01-11 02:53:18', '2026-01-11 02:53:18', '15114949450', 'wangqi');
INSERT INTO `user` VALUES (2, 'zhangwuji', '123456', '2026-01-04 18:16:39', '2026-01-04 18:16:39', '13992277779', 'ZWJ');
INSERT INTO `user` VALUES (3, 'yangxiao', '123456', '2026-01-04 18:16:39', '2026-01-04 18:16:39', '13992277780', 'YX');
INSERT INTO `user` VALUES (19, 'wq111', 'wq123456', '2026-01-11 19:53:46', '2026-01-11 19:53:46', '13777777777', '王奇');

SET FOREIGN_KEY_CHECKS = 1;
