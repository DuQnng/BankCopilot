CREATE TABLE `transaction_record` (
                                      `id` bigint NOT NULL AUTO_INCREMENT,
                                      `account_id` bigint NOT NULL,
                                      `counterparty_account_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                                      `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                      `amount` decimal(12,2) NOT NULL,
                                      `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                                      `trade_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                      PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC




INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (1, 1, '6222000098765432', '收入', 5000.00, '工资', '2026-01-11 18:47:17');

INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (2, 1, '6222000076543210', '支出', -200.00, '购物', '2026-01-11 18:47:17');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (7, 1, '6222000076543210', '支出', -2000.00, '有钱！大把挥霍！', '2026-01-11 19:31:54');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (8, 4, '6222000012345678', '收入', 2000.00, '有钱！大把挥霍！', '2026-01-11 19:31:54');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (9, 1, '6222000076543210', '支出', -111.00, '22222', '2026-01-17 09:32:14');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (10, 4, '6222000012345678', '收入', 111.00, '22222', '2026-01-17 09:32:14');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (11, 1, '6222000076543210', '支出', -200.00, '111', '2026-01-17 10:09:19');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (12, 4, '6222000012345678', '收入', 200.00, '111', '2026-01-17 10:09:19');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (13, 1, '6222000076543210', '支出', -123.00, '转账逻辑测试', '2026-01-17 10:21:08');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (14, 4, '6222000012345678', '收入', 123.00, '转账逻辑测试', '2026-01-17 10:21:08');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (15, 1, '6222000076543210', '支出', -123.00, '转账逻辑测试', '2026-01-17 10:21:41');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (16, 4, '6222000012345678', '收入', 123.00, '转账逻辑测试', '2026-01-17 10:21:41');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (17, 1, '6222000076543210', '支出', -123.00, '转账逻辑测试222', '2026-01-17 10:21:49');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (18, 4, '6222000012345678', '收入', 123.00, '转账逻辑测试222', '2026-01-17 10:21:49');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (19, 1, '6222000076543210', '支出', -123.00, '逻辑测试第二次', '2026-01-17 10:25:32');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (20, 4, '6222000012345678', '收入', 123.00, '逻辑测试第二次', '2026-01-17 10:25:32');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (21, 1, '6222000076543210', '支出', -60.00, '测试逻辑第3次', '2026-01-17 10:27:54');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (22, 4, '6222000012345678', '收入', 60.00, '测试逻辑第3次', '2026-01-17 10:27:54');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (23, 1, '6222000076543210', '支出', -73.00, '', '2026-03-08 22:43:36');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (24, 4, '6222000012345678', '收入', 73.00, '', '2026-03-08 22:43:36');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (25, 1, '6222000076543210', '支出', -300.00, '', '2026-03-08 23:09:09');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (26, 4, '6222000012345678', '收入', 300.00, '', '2026-03-08 23:09:09');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (27, 1, '6222000076543210', '支出', -250.00, '', '2026-03-09 00:02:27');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (28, 4, '6222000012345678', '收入', 250.00, '', '2026-03-09 00:02:27');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (29, 1, '6222000076543210', '支出', -300.00, '测试5566', '2026-03-17 12:58:06');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (30, 4, '6222000012345678', '收入', 300.00, '测试5566', '2026-03-17 12:58:06');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (31, 1, '6222000076543210', '支出', -487.00, '测试3333', '2026-03-17 14:19:40');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (32, 4, '6222000012345678', '收入', 487.00, '测试3333', '2026-03-17 14:19:40');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (33, 1, '6222000043210987', '支出', -10000.00, '风险测试', '2026-03-22 16:14:21');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (34, 3, '6222000012345678', '收入', 10000.00, '风险测试', '2026-03-22 16:14:21');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (35, 3, '6222000012345678', '支出', -10000.00, '大额支出', '2026-04-07 14:37:33');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (36, 1, '6222000043210987', '收入', 10000.00, '大额支出', '2026-04-07 14:37:33');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (37, 3, '6222000012345678', '支出', -100.00, '小额', '2026-04-07 14:37:42');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (38, 1, '6222000043210987', '收入', 100.00, '小额', '2026-04-07 14:37:42');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (39, 3, '6222000012345678', '支出', -1000.00, '小额', '2026-04-07 14:37:52');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (40, 1, '6222000043210987', '收入', 1000.00, '小额', '2026-04-07 14:37:52');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (41, 1, '6222000098765432', '支出', -300.00, '弟弟', '2026-04-23 10:55:17');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (42, 2, '6222000012345678', '收入', 300.00, '弟弟', '2026-04-23 10:55:17');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (43, 1, '6222000098765432', '支出', -300.00, '弟弟的午餐费', '2026-04-21 10:55:38');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (44, 2, '6222000012345678', '收入', 300.00, '弟弟的午餐费', '2026-04-21 10:55:38');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (45, 1, '6222000098765432', '支出', -100.00, '晚餐费', '2026-04-17 11:04:47');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (46, 2, '6222000012345678', '收入', 100.00, '晚餐费', '2026-04-17 11:04:47');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (47, 1, '6222000043210987', '支出', -3000.00, '团建费', '2026-03-23 11:05:44');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (48, 3, '6222000012345678', '收入', 3000.00, '团建费', '2026-03-23 11:05:44');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (49, 1, '6222000076543210', '支出', -3000.00, '', '2026-04-23 11:06:08');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (50, 4, '6222000012345678', '收入', 3000.00, '', '2026-04-23 11:06:08');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (51, 1, '6222000098765432', '支出', -100.00, '晚餐费', '2026-04-23 11:22:44');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (52, 2, '6222000012345678', '收入', 100.00, '晚餐费', '2026-04-23 11:22:44');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (53, 1, '6222000098765432', '支出', -300.00, '午餐费', '2026-04-23 11:44:56');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (54, 2, '6222000012345678', '收入', 300.00, '午餐费', '2026-04-23 11:44:56');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (55, 1, '6222000098765432', '支出', -100.00, '晚餐费', '2026-04-23 11:45:23');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (56, 2, '6222000012345678', '收入', 100.00, '晚餐费', '2026-04-23 11:45:23');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (57, 1, '6222000098765432', '支出', -100.00, '晚餐费', '2026-04-23 11:46:52');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (58, 2, '6222000012345678', '收入', 100.00, '晚餐费', '2026-04-23 11:46:52');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (59, 1, '6222000043210987', '支出', -3000.00, '团建费', '2026-04-23 11:48:10');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (60, 3, '6222000012345678', '收入', 3000.00, '团建费', '2026-04-23 11:48:10');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (61, 1, '6222000098765432', '支出', -100.00, '晚餐费', '2026-04-23 12:03:03');
INSERT INTO tlias.transaction_record (id, account_id, counterparty_account_no, type, amount, description, trade_time) VALUES (62, 2, '6222000012345678', '收入', 100.00, '晚餐费', '2026-04-23 12:03:03');
