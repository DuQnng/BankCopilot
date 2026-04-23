/*
 收款人管理表
 用途：保存“当前登录用户”的常用收款对象
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `payee_contact`;
CREATE TABLE `payee_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `payee_name` varchar(64) NOT NULL COMMENT '收款人姓名',
  `account_no` varchar(32) NOT NULL COMMENT '收款人银行卡号',
  `alias` varchar(64) DEFAULT NULL COMMENT '备注/昵称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_account_no` (`user_id`, `account_no`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='常用收款人表';

-- 示例数据（可按需删除）
INSERT INTO `payee_contact` (`user_id`, `payee_name`, `account_no`, `alias`) VALUES
(1, '张无忌', '6222000098765432', '武当小张'),
(1, '杨逍',   '6222000043210987', '明教老杨');

SET FOREIGN_KEY_CHECKS = 1;

