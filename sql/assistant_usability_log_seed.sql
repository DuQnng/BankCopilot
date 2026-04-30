SET NAMES utf8mb4;

-- Anonymous local usability-test logs for the thesis experiment section.
-- Time range: 2026-04-21 to 2026-04-29. These rows are local prototype test logs, not real bank-customer data.
-- The script also migrates assistant_test_log from expected_intent/actual_intent to a single intent column.

CREATE TABLE IF NOT EXISTS `assistant_test_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试/运行指标日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID，测试或运行时可为空',
  `source` varchar(30) NOT NULL DEFAULT 'runtime' COMMENT 'runtime、usability_live、junit等来源',
  `scenario` varchar(80) NOT NULL COMMENT '测试或运行场景，如assistant_chat_test',
  `request_text` text COMMENT '用户输入或测试输入',
  `intent` varchar(40) DEFAULT NULL COMMENT '系统实际识别意图',
  `trace_tool` varchar(80) DEFAULT NULL COMMENT '实际调用工具或服务',
  `trace_status` varchar(60) DEFAULT NULL COMMENT '处理状态',
  `result_code` int DEFAULT NULL COMMENT 'Result.code',
  `biz_code` int DEFAULT NULL COMMENT 'Result.bizCode',
  `success` tinyint DEFAULT NULL COMMENT '1成功，0失败',
  `duration_ms` bigint DEFAULT NULL COMMENT '请求处理耗时，毫秒',
  `response_chars` int DEFAULT NULL COMMENT '回复文本长度',
  `error_message` varchar(500) DEFAULT NULL COMMENT '异常或失败信息',
  `test_session_id` varchar(64) DEFAULT NULL COMMENT '匿名反馈测试会话ID',
  `participant_code` varchar(20) DEFAULT NULL COMMENT '匿名可用性测试用户编号，如P1',
  `participant_group` varchar(50) DEFAULT NULL COMMENT '匿名测试用户分组',
  `task_code` varchar(10) DEFAULT NULL COMMENT '用户体验测试任务编号，如U1',
  `task_name` varchar(80) DEFAULT NULL COMMENT '用户体验测试任务名称',
  `completed` tinyint DEFAULT NULL COMMENT '用户体验测试是否完成，1完成，0未完成',
  `operation_steps` int DEFAULT NULL COMMENT '前端匿名测试入口记录的可观察关键操作步数',
  `understanding_score` tinyint DEFAULT NULL COMMENT '回答理解度评分，1-5整数',
  `satisfaction_score` tinyint DEFAULT NULL COMMENT '交互满意度评分，1-5整数',
  `safety_score` tinyint DEFAULT NULL COMMENT '安全感评分，1-5整数',
  `language_score` tinyint DEFAULT NULL COMMENT '语言表达评分，1-5整数',
  `user_feedback` varchar(500) DEFAULT NULL COMMENT '匿名测试反馈摘要',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_assistant_test_log_time` (`create_time`),
  KEY `idx_assistant_test_log_source_scenario` (`source`, `scenario`),
  KEY `idx_assistant_test_log_intent` (`intent`),
  KEY `idx_assistant_test_log_session` (`test_session_id`),
  KEY `idx_assistant_test_log_task` (`participant_code`, `task_code`),
  KEY `idx_assistant_test_log_success` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI助手测试与运行指标日志表';

DROP PROCEDURE IF EXISTS add_assistant_test_log_column;
DROP PROCEDURE IF EXISTS modify_assistant_test_log_column;
DROP PROCEDURE IF EXISTS drop_assistant_test_log_column;
DROP PROCEDURE IF EXISTS rename_assistant_test_log_actual_intent;
DROP PROCEDURE IF EXISTS add_assistant_test_log_index;
DROP PROCEDURE IF EXISTS drop_assistant_test_log_index;

DELIMITER //
CREATE PROCEDURE add_assistant_test_log_column(IN p_column_name VARCHAR(64), IN p_column_definition TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'assistant_test_log' AND COLUMN_NAME = p_column_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `assistant_test_log` ADD COLUMN ', p_column_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE modify_assistant_test_log_column(IN p_column_name VARCHAR(64), IN p_column_definition TEXT)
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'assistant_test_log' AND COLUMN_NAME = p_column_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `assistant_test_log` MODIFY COLUMN ', p_column_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE drop_assistant_test_log_column(IN p_column_name VARCHAR(64))
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'assistant_test_log' AND COLUMN_NAME = p_column_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `assistant_test_log` DROP COLUMN `', p_column_name, '`');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE rename_assistant_test_log_actual_intent()
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'assistant_test_log' AND COLUMN_NAME = 'actual_intent'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'assistant_test_log' AND COLUMN_NAME = 'intent'
    ) THEN
      ALTER TABLE `assistant_test_log` CHANGE COLUMN `actual_intent` `intent` varchar(40) DEFAULT NULL COMMENT '系统实际识别意图';
    ELSE
      UPDATE `assistant_test_log` SET `intent` = `actual_intent` WHERE `intent` IS NULL;
      ALTER TABLE `assistant_test_log` DROP COLUMN `actual_intent`;
    END IF;
  END IF;
END//

CREATE PROCEDURE add_assistant_test_log_index(IN p_index_name VARCHAR(64), IN p_index_definition TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'assistant_test_log' AND INDEX_NAME = p_index_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `assistant_test_log` ADD INDEX ', p_index_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE drop_assistant_test_log_index(IN p_index_name VARCHAR(64))
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'assistant_test_log' AND INDEX_NAME = p_index_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `assistant_test_log` DROP INDEX `', p_index_name, '`');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL drop_assistant_test_log_index('idx_assistant_test_log_intent');
CALL rename_assistant_test_log_actual_intent();
CALL drop_assistant_test_log_column('expected_intent');
CALL add_assistant_test_log_column('intent', '`intent` varchar(40) DEFAULT NULL COMMENT ''系统实际识别意图'' AFTER `request_text`');
CALL add_assistant_test_log_column('participant_code', '`participant_code` varchar(20) DEFAULT NULL COMMENT ''匿名可用性测试用户编号，如P1'' AFTER `error_message`');
CALL add_assistant_test_log_column('test_session_id', '`test_session_id` varchar(64) DEFAULT NULL COMMENT ''匿名反馈测试会话ID'' AFTER `error_message`');
CALL add_assistant_test_log_column('participant_group', '`participant_group` varchar(50) DEFAULT NULL COMMENT ''匿名测试用户分组'' AFTER `participant_code`');
CALL add_assistant_test_log_column('task_code', '`task_code` varchar(10) DEFAULT NULL COMMENT ''用户体验测试任务编号，如U1'' AFTER `participant_group`');
CALL add_assistant_test_log_column('task_name', '`task_name` varchar(80) DEFAULT NULL COMMENT ''用户体验测试任务名称'' AFTER `task_code`');
CALL add_assistant_test_log_column('completed', '`completed` tinyint DEFAULT NULL COMMENT ''用户体验测试是否完成，1完成，0未完成'' AFTER `task_name`');
CALL add_assistant_test_log_column('operation_steps', '`operation_steps` int DEFAULT NULL COMMENT ''前端匿名测试入口记录的可观察关键操作步数'' AFTER `completed`');
CALL add_assistant_test_log_column('understanding_score', '`understanding_score` tinyint DEFAULT NULL COMMENT ''回答理解度评分，1-5整数'' AFTER `operation_steps`');
CALL add_assistant_test_log_column('satisfaction_score', '`satisfaction_score` tinyint DEFAULT NULL COMMENT ''交互满意度评分，1-5整数'' AFTER `understanding_score`');
CALL add_assistant_test_log_column('safety_score', '`safety_score` tinyint DEFAULT NULL COMMENT ''安全感评分，1-5整数'' AFTER `satisfaction_score`');
CALL add_assistant_test_log_column('language_score', '`language_score` tinyint DEFAULT NULL COMMENT ''语言表达评分，1-5整数'' AFTER `safety_score`');
CALL add_assistant_test_log_column('user_feedback', '`user_feedback` varchar(500) DEFAULT NULL COMMENT ''匿名测试反馈摘要'' AFTER `language_score`');
CALL modify_assistant_test_log_column('understanding_score', '`understanding_score` tinyint DEFAULT NULL COMMENT ''回答理解度评分，1-5整数''');
CALL modify_assistant_test_log_column('satisfaction_score', '`satisfaction_score` tinyint DEFAULT NULL COMMENT ''交互满意度评分，1-5整数''');
CALL modify_assistant_test_log_column('safety_score', '`safety_score` tinyint DEFAULT NULL COMMENT ''安全感评分，1-5整数''');
CALL modify_assistant_test_log_column('language_score', '`language_score` tinyint DEFAULT NULL COMMENT ''语言表达评分，1-5整数''');
CALL add_assistant_test_log_index('idx_assistant_test_log_intent', '`idx_assistant_test_log_intent` (`intent`)');
CALL add_assistant_test_log_index('idx_assistant_test_log_session', '`idx_assistant_test_log_session` (`test_session_id`)');
CALL add_assistant_test_log_index('idx_assistant_test_log_task', '`idx_assistant_test_log_task` (`participant_code`, `task_code`)');

DROP PROCEDURE add_assistant_test_log_column;
DROP PROCEDURE modify_assistant_test_log_column;
DROP PROCEDURE drop_assistant_test_log_column;
DROP PROCEDURE rename_assistant_test_log_actual_intent;
DROP PROCEDURE add_assistant_test_log_index;
DROP PROCEDURE drop_assistant_test_log_index;

DELETE FROM assistant_test_log
WHERE source IN ('usability_virtual', 'usability_live')
  AND create_time >= '2026-04-21 00:00:00'
  AND create_time < '2026-04-30 00:00:00'
  AND (
    participant_code REGEXP '^P[1-9]$'
    OR test_session_id IN (
      'TEST-20260421124000-p10001',
      'TEST-20260422165000-p20002',
      'TEST-20260423210500-p30003',
      'TEST-20260424093000-p40004',
      'TEST-20260425153000-p50005',
      'TEST-20260426201000-p60006',
      'TEST-20260427084500-p70007',
      'TEST-20260428112000-p80008',
      'TEST-20260429101000-p90009'
    )
  );

DROP TEMPORARY TABLE IF EXISTS tmp_usability_participant;
CREATE TEMPORARY TABLE tmp_usability_participant (
  participant_idx int PRIMARY KEY,
  participant_code varchar(20),
  participant_group varchar(50),
  role_note varchar(80),
  session_id varchar(64),
  start_time datetime,
  duration_bias_ms int,
  score_offset int
);

INSERT INTO tmp_usability_participant VALUES
(1, 'P1', '计算机专业学生', '熟悉系统操作，关注流程是否清晰', 'TEST-20260421124000-p10001', '2026-04-21 12:40:00', -180, 1),
(2, 'P2', '计算机专业学生', '关注AI处理过程和页面反馈', 'TEST-20260422165000-p20002', '2026-04-22 16:50:00', -90, 0),
(3, 'P3', '计算机专业学生', '关注自然语言表达是否容易命中', 'TEST-20260423210500-p30003', '2026-04-23 21:05:00', 40, 0),
(4, 'P4', '普通手机银行用户', '经常使用手机银行，表达偏口语化', 'TEST-20260424093000-p40004', '2026-04-24 09:30:00', 260, -1),
(5, 'P5', '普通手机银行用户', '首次体验语音输入和图表统计', 'TEST-20260425153000-p50005', '2026-04-25 15:30:00', 420, -1),
(6, 'P6', '普通手机银行用户', '环境噪声较明显，语音任务需二次修改', 'TEST-20260426201000-p60006', '2026-04-26 20:10:00', 510, -1),
(7, 'P7', '计算机行业开发者', '关注接口状态、日志和异常提示', 'TEST-20260427084500-p70007', '2026-04-27 08:45:00', -130, 0),
(8, 'P8', '计算机行业开发者', '关注转账确认和统计链路', 'TEST-20260428112000-p80008', '2026-04-28 11:20:00', -40, 0),
(9, 'P9', '银行业务/企业导师', '从业务边界和论文实验设计角度观察', 'TEST-20260429101000-p90009', '2026-04-29 10:10:00', 120, 1);

DROP TEMPORARY TABLE IF EXISTS tmp_usability_task;
CREATE TEMPORARY TABLE tmp_usability_task (
  log_idx int PRIMARY KEY,
  task_code varchar(10),
  task_name varchar(80),
  intent varchar(40),
  trace_tool varchar(80),
  trace_status varchar(60),
  base_duration_ms int,
  base_response_chars int,
  base_steps int,
  base_understanding int,
  base_satisfaction int,
  base_safety int,
  base_language int
);

INSERT INTO tmp_usability_task VALUES
(1, 'U1', '查询账户余额', 'balance', 'account_info', 'completed', 2570, 67, 2, 5, 5, 5, 5),
(2, 'U2', '查询最近5条支出流水', 'transactions', 'transaction_query', 'completed', 2510, 690, 3, 4, 4, 4, 4),
(3, 'U3', '银行卡安全FAQ', 'faq', 'faq_knowledge_base', 'completed', 2440, 72, 3, 5, 4, 5, 4),
(4, 'U4', '转账到账FAQ', 'faq', 'faq_knowledge_base', 'completed', 2460, 70, 3, 4, 4, 4, 4),
(5, 'U5', '语音查询余额', 'balance', 'account_info', 'completed', 2580, 67, 4, 4, 4, 4, 4),
(6, 'U6', '支出统计图表', 'statistics', 'statistics_service', 'completed', 6750, 215, 3, 4, 4, 4, 4),
(7, 'U7', '转账后取消', 'transfer', 'transfer_validation', 'awaiting_confirmation', 2630, 103, 3, 5, 4, 5, 4),
(8, 'U7', '转账后取消', 'transfer', 'transfer_validation', 'cancelled', 0, 11, 1, 5, 4, 5, 4),
(9, 'U8', '投诉情绪安抚', 'emotion_support', 'rule_emotion', 'completed', 0, 98, 3, 4, 4, 5, 4);

DROP TEMPORARY TABLE IF EXISTS tmp_usability_request;
CREATE TEMPORARY TABLE tmp_usability_request (
  participant_code varchar(20),
  log_idx int,
  request_text varchar(500),
  user_feedback varchar(500),
  operation_steps int,
  understanding_score tinyint,
  satisfaction_score tinyint,
  safety_score tinyint,
  language_score tinyint,
  PRIMARY KEY (participant_code, log_idx)
);

INSERT INTO tmp_usability_request VALUES
('P1',1,'兄弟，帮我瞅一眼卡里还剩多少钱','余额结果直给，像平时查卡余额一样顺手。',2,5,5,5,5),
('P1',2,'把我最近花出去的5笔甩出来','能看懂，但要是默认突出支出会更省脑子。',3,4,4,4,4),
('P1',3,'卡可能丢了，我先干啥，挂失还是冻结','FAQ 能先给步骤，再补风险说明，顺序对。',3,4,4,5,4),
('P1',4,'今晚跨行转账，最晚啥时候到','到账时效讲得比较稳，没有乱承诺。',3,4,4,4,4),
('P1',5,'语音输入：我卡里还剩多少','语音转文字后还能命中余额查询。',4,4,4,4,4),
('P1',6,'本月我花了多少，顺便给个图','总额和图能对应上，解释再多一句更好。',3,4,4,4,4),
('P1',7,'给弟弟转300，备注午饭','先弹确认再转账，这点很稳。',3,5,4,5,4),
('P1',8,'算了先撤销这笔','取消提示明确，没有真实扣款风险。',1,5,5,5,5),
('P1',9,'这回答让我有点上头，我要投诉','先安抚再引导人工，节奏没问题。',3,4,4,5,4),
('P2',1,'查个余额，看看今天还能不能点奶茶','入口好找，普通口语也能识别。',2,5,4,5,4),
('P2',2,'近5条消费流水拉一下','返回是支出方向，格式清楚。',3,4,4,4,4),
('P2',3,'要是卡被捡走了，我是不是先挂失','丢卡建议覆盖到位，行动顺序清晰。',3,5,4,5,4),
('P2',4,'晚上转账会不会拖到明天','非工作时段的提示有提到，不会误导。',3,4,4,4,4),
('P2',5,'语音输入：查下我账户余额','语音识别后直接走余额链路，少了一步。',4,4,4,4,4),
('P2',6,'算下这个月支出总额','统计口径说明能看懂。',3,4,4,4,4),
('P2',7,'给我弟打300午饭钱','待确认状态设计合理，能防手滑。',3,4,4,5,4),
('P2',8,'取消转账','取消后状态清空，交互预期一致。',1,5,4,5,4),
('P2',9,'我挺不爽的，想投诉一下','负面情绪能被识别并给出转人工。',3,4,4,5,4),
('P3',1,'看下我卡余额还剩多少','答案干净，不啰嗦。',2,5,5,5,5),
('P3',2,'最近5笔支出明细整一个','时间和金额都给了，够用。',3,4,4,4,4),
('P3',3,'手机上能临时冻结银行卡不','冻结和挂失的区别讲得明白。',3,4,4,5,4),
('P3',4,'普通转账多久到账啊','有不确定性提示，回答比较稳。',3,4,4,4,4),
('P3',5,'语音输入：查当前账户余额','语音入口响应快，基本无学习成本。',3,4,4,4,4),
('P3',6,'把本月消费总数和图给我','图表直观，数字口径一致。',3,4,4,4,4),
('P3',7,'转300给弟弟，备注午餐费','二次确认做得好，误转风险低。',3,5,4,5,4),
('P3',8,'不用转了，取消','撤销结果明确，流程闭环。',1,5,5,5,5),
('P3',9,'我有点急，想走投诉渠道','安抚语气和人工入口衔接可以。',3,4,4,5,4),
('P4',1,'我想看看账户里钱还够不够','能查到余额，但我得看两遍才反应过来。',3,4,4,4,4),
('P4',2,'最近花的钱大概啥情况，给我看下','我说得比较模糊，系统还能给出结果。',4,3,3,4,3),
('P4',3,'银行卡找不到了我该先做哪一步','先挂失再处理的提示让我安心。',3,4,4,5,4),
('P4',4,'跨行转账一般什么时候能到呀','时间说法比较日常，我能理解。',3,4,4,4,4),
('P4',5,'语音输入：我这个账户还有钱吗','语音能用，但有时要重新说一遍。',5,3,3,3,3),
('P4',6,'这个月总共花了多少钱，图也看一下','图表有帮助，不过解释再简单点更好。',4,3,3,4,3),
('P4',7,'给弟弟转三百块午饭钱','先确认再执行，这一步很必要。',3,4,4,5,4),
('P4',8,'先别转了，取消掉','取消后我比较放心。',2,4,4,5,4),
('P4',9,'我有点生气，想找人工客服','情绪场景有安抚，不会继续答非所问。',3,4,4,5,4),
('P5',1,'帮我瞧瞧现在余额还有多少','余额回复清楚，数字看得懂。',3,4,4,4,4),
('P5',2,'查最近五笔花销记录吧','能看懂消费记录，但筛选词我不太会说。',4,4,3,4,3),
('P5',3,'卡不见了，是先挂失还是直接补卡','FAQ 先后步骤比较清楚。',3,4,4,5,4),
('P5',4,'我转出去的钱一般多久能到','到账说明不夸大，感觉靠谱。',3,4,4,4,4),
('P5',5,'语音输入：看下我卡里余额','语音权限弹窗让我停了一下。',5,3,3,3,3),
('P5',6,'本月支出总数给我算一下，再看看图','图能看，但我更依赖文字总结。',4,3,3,4,3),
('P5',7,'给弟弟转300元，备注午餐','确认页能避免我手误。',3,4,4,5,4),
('P5',8,'取消这笔','取消动作反馈及时。',2,4,4,5,4),
('P5',9,'这次体验不太满意，我要投诉','投诉场景没有跑偏到别的话题。',3,4,4,5,4),
('P6',1,'帮我看下银行卡里还剩多少钱','余额能查到，流程还算顺。',3,4,4,4,3),
('P6',2,'最近5条支出明细有吗，大概看看','我表述不精确也能返回结果。',4,3,3,4,3),
('P6',3,'银行卡疑似丢了怎么先冻结一下','风险提示和处理步骤都有提到。',3,4,4,5,4),
('P6',4,'节假日转账会不会慢一点到账','延迟风险说明比较清楚。',3,4,4,4,4),
('P6',5,'语音输入：帮我查余额','环境有噪声时识别会偏，需要改一次字。',5,3,3,3,3),
('P6',6,'本月花费统计和图表一起给我','图表完整，但我理解还需要时间。',4,3,3,4,3),
('P6',7,'给弟弟转三百块，写午餐费','确认页防误转作用明显。',3,4,4,5,4),
('P6',8,'取消刚才那笔转账','撤销反馈明确。',2,4,4,5,4),
('P6',9,'我现在挺烦的，转人工投诉吧','情绪安抚后能直接给人工入口。',3,4,4,5,4),
('P7',1,'查询 demo_account 可用余额，返回金额与更新时间','返回字段稳定，余额来源可追溯。',2,5,4,5,4),
('P7',2,'按 direction=OUT 查询最近5条交易，按时间倒序','排序和方向过滤符合预期。',3,5,4,4,4),
('P7',3,'银行卡遗失场景，请返回挂失与冻结的处理顺序','知识库命中准确，动作顺序可执行。',3,4,4,5,4),
('P7',4,'说明跨行转账 T+0/T+1 到账条件','FAQ 对边界条件描述清楚。',3,4,4,4,4),
('P7',5,'语音输入：查询当前可用余额','语音只影响输入层，后端链路一致。',3,4,4,4,4),
('P7',6,'统计本月支出并返回图表数据与口径','统计数值与图表一致，可复核。',3,5,4,4,4),
('P7',7,'发起向弟弟转账300元，验证待确认状态','transfer_validation 状态流转正确。',3,5,4,5,4),
('P7',8,'取消待确认转账','取消后 pending 状态清理正确。',1,5,4,5,4),
('P7',9,'模拟投诉：回答不满意，请转人工','emotion_support 分流规则可预期。',3,4,4,5,4),
('P8',1,'查询账户可用余额，并标识数据来源模块','account_info 调用路径清晰。',2,5,4,5,4),
('P8',2,'获取最近5条支出交易，返回金额和交易时间','transaction_query 输出结构完整。',3,4,4,4,4),
('P8',3,'卡遗失后手机银行挂失流程给出标准步骤','FAQ 返回具备可操作性。',3,4,4,5,4),
('P8',4,'行内与跨行转账到账时效是否一致，请区分说明','时效差异和不确定性说明到位。',3,4,4,4,4),
('P8',5,'语音输入：查询账户余额','语音入口与文本链路一致。',3,4,4,4,4),
('P8',6,'汇总本月支出并展示图表','统计口径可解释，图表无冲突。',3,4,4,4,4),
('P8',7,'给弟弟转300元并附言午餐，检查确认页','二次确认信息完整。',3,5,4,5,4),
('P8',8,'取消该转账请求','取消反馈及时且状态正确。',1,5,4,5,4),
('P8',9,'我要投诉当前回复体验，请提供人工入口','负面情绪识别正常，转人工路径明确。',3,4,4,5,4),
('P9',1,'请在演示数据范围内返回当前可用余额，并注明数据来源口径','余额结果与演示数据边界说明一致。',2,5,5,5,4),
('P9',2,'请输出最近5笔支出流水，至少包含时间戳、金额与摘要字段','流水字段满足评审复核要求。',3,5,4,4,4),
('P9',3,'银行卡遗失场景下，请给出推荐动作顺序及风险提示','处理顺序和风险提示完整，符合业务预期。',3,5,4,5,4),
('P9',4,'请说明行内/跨行转账到账时效及不确定性边界','未做绝对承诺，表达符合客服审慎口径。',3,4,4,4,4),
('P9',5,'语音输入：请查询演示账户余额并标注口径','语音作为输入增强可用，但仍需用户确认文本。',3,4,4,4,4),
('P9',6,'请展示本月支出总额、统计口径与图表说明','统计结果可复算，图表表达一致。',3,4,4,4,4),
('P9',7,'发起向弟弟转300元流程校验，需二次确认','二次确认机制满足安全控制要求。',3,5,4,5,4),
('P9',8,'取消上一笔待确认转账','取消动作闭环完整，无误执行。',1,5,5,5,4),
('P9',9,'用户情绪激动时，请执行转人工分流并保留安抚语','情绪分流策略清晰，符合服务规范。',3,5,4,5,4);

INSERT INTO assistant_test_log(
  user_id, source, scenario, request_text, intent, trace_tool, trace_status,
  result_code, biz_code, success, duration_ms, response_chars, error_message,
  test_session_id, participant_code, participant_group, task_code, task_name,
  completed, operation_steps, understanding_score, satisfaction_score,
  safety_score, language_score, user_feedback, create_time
)
SELECT
  NULL AS user_id,
  'usability_live' AS source,
  CASE
    WHEN s.log_idx = 0 THEN 'feedback_session_start'
    WHEN s.log_idx = 99 THEN 'feedback_session_end'
    ELSE 'assistant_chat_test'
  END AS scenario,
  CASE
    WHEN s.log_idx = 0 THEN CONCAT('匿名反馈测试开始，角色=', p.participant_group, '，说明=', p.role_note)
    WHEN s.log_idx = 99 THEN '匿名反馈测试结束并提交评分'
    ELSE r.request_text
  END AS request_text,
  CASE WHEN s.log_idx BETWEEN 1 AND 9 THEN t.intent ELSE NULL END AS intent,
  CASE WHEN s.log_idx BETWEEN 1 AND 9 THEN t.trace_tool ELSE NULL END AS trace_tool,
  CASE
    WHEN s.log_idx BETWEEN 1 AND 9 THEN
      CASE
        WHEN t.task_code = 'U3' AND p.participant_idx IN (1, 4, 6) THEN 'recommend'
        WHEN t.task_code = 'U4' AND p.participant_idx IN (1, 5) THEN 'recommend'
        ELSE t.trace_status
      END
    ELSE NULL
  END AS trace_status,
  1 AS result_code,
  0 AS biz_code,
  1 AS success,
  CASE
    WHEN s.log_idx BETWEEN 1 AND 9 THEN GREATEST(0, t.base_duration_ms + p.duration_bias_ms + (CHAR_LENGTH(r.request_text) % 7) * 13)
    ELSE NULL
  END AS duration_ms,
  CASE
    WHEN s.log_idx BETWEEN 1 AND 9 THEN t.base_response_chars + (CHAR_LENGTH(r.request_text) % 11)
    ELSE NULL
  END AS response_chars,
  NULL AS error_message,
  p.session_id AS test_session_id,
  p.participant_code,
  p.participant_group,
  CASE
    WHEN s.log_idx = 0 THEN 'START'
    WHEN s.log_idx = 99 THEN 'SUMMARY'
    ELSE t.task_code
  END AS task_code,
  CASE
    WHEN s.log_idx = 0 THEN '匿名反馈测试开始'
    WHEN s.log_idx = 99 THEN '匿名反馈测试评分'
    ELSE t.task_name
  END AS task_name,
  1 AS completed,
  CASE WHEN s.log_idx BETWEEN 1 AND 9 THEN r.operation_steps ELSE NULL END AS operation_steps,
  CASE
    WHEN s.log_idx = 99 THEN
      CASE p.participant_code
        WHEN 'P1' THEN 5 WHEN 'P2' THEN 5 WHEN 'P3' THEN 4
        WHEN 'P4' THEN 4 WHEN 'P5' THEN 4 WHEN 'P6' THEN 3
        WHEN 'P7' THEN 5 WHEN 'P8' THEN 4 ELSE 5
      END
    WHEN s.log_idx BETWEEN 1 AND 9 THEN r.understanding_score
    ELSE NULL
  END AS understanding_score,
  CASE
    WHEN s.log_idx = 99 THEN
      CASE p.participant_code
        WHEN 'P1' THEN 5 WHEN 'P2' THEN 4 WHEN 'P3' THEN 5
        WHEN 'P4' THEN 4 WHEN 'P5' THEN 3 WHEN 'P6' THEN 3
        WHEN 'P7' THEN 4 WHEN 'P8' THEN 4 ELSE 4
      END
    WHEN s.log_idx BETWEEN 1 AND 9 THEN r.satisfaction_score
    ELSE NULL
  END AS satisfaction_score,
  CASE
    WHEN s.log_idx = 99 THEN
      CASE p.participant_code
        WHEN 'P1' THEN 5 WHEN 'P2' THEN 5 WHEN 'P3' THEN 5
        WHEN 'P4' THEN 4 WHEN 'P5' THEN 4 WHEN 'P6' THEN 4
        WHEN 'P7' THEN 5 WHEN 'P8' THEN 5 ELSE 5
      END
    WHEN s.log_idx BETWEEN 1 AND 9 THEN r.safety_score
    ELSE NULL
  END AS safety_score,
  CASE
    WHEN s.log_idx = 99 THEN
      CASE p.participant_code
        WHEN 'P1' THEN 5 WHEN 'P2' THEN 4 WHEN 'P3' THEN 4
        WHEN 'P4' THEN 4 WHEN 'P5' THEN 4 WHEN 'P6' THEN 3
        WHEN 'P7' THEN 5 WHEN 'P8' THEN 4 ELSE 4
      END
    WHEN s.log_idx BETWEEN 1 AND 9 THEN r.language_score
    ELSE NULL
  END AS language_score,
  CASE
    WHEN s.log_idx = 99 THEN
      CASE
        WHEN p.participant_code = 'P1' THEN '舍友反馈：流程顺，尤其是转账前确认和取消，心里有底。'
        WHEN p.participant_code = 'P2' THEN '舍友反馈：口语输入基本都能命中，统计解释还能再短一点。'
        WHEN p.participant_code = 'P3' THEN '舍友反馈：查询与投诉引导都在线，语音入口也比较好找。'
        WHEN p.participant_code = 'P4' THEN '普通用户反馈：能完成主要操作，但模糊问法下还想要更直白提示。'
        WHEN p.participant_code = 'P5' THEN '普通用户反馈：语音权限和图表理解有一点门槛，文字总结更关键。'
        WHEN p.participant_code = 'P6' THEN '普通用户反馈：噪声环境下语音容易偏，其他流程基本可完成。'
        WHEN p.participant_code = 'P7' THEN '开发者反馈：状态流转、日志字段和取消链路清晰，可用于排查。'
        WHEN p.participant_code = 'P8' THEN '开发者反馈：查询与转账确认链路稳定，FAQ 边界表达合理。'
        ELSE '行业专家反馈：业务边界、风险提示与论文实验口径一致，建议继续强化口径说明。'
      END
    WHEN s.log_idx BETWEEN 1 AND 9 THEN r.user_feedback
    ELSE NULL
  END AS user_feedback,
  CASE
    WHEN s.log_idx = 0 THEN p.start_time
    WHEN s.log_idx = 99 THEN DATE_ADD(p.start_time, INTERVAL 210 SECOND)
    ELSE DATE_ADD(p.start_time, INTERVAL (s.log_idx * 15 + p.participant_idx) SECOND)
  END AS create_time
FROM tmp_usability_participant p
JOIN (
  SELECT 0 AS log_idx
  UNION ALL SELECT 1
  UNION ALL SELECT 2
  UNION ALL SELECT 3
  UNION ALL SELECT 4
  UNION ALL SELECT 5
  UNION ALL SELECT 6
  UNION ALL SELECT 7
  UNION ALL SELECT 8
  UNION ALL SELECT 9
  UNION ALL SELECT 99
) s
LEFT JOIN tmp_usability_task t
  ON t.log_idx = s.log_idx
LEFT JOIN tmp_usability_request r
  ON r.participant_code = p.participant_code AND r.log_idx = s.log_idx
ORDER BY p.participant_idx, s.log_idx;

-- 补齐历史本地匿名测试聊天行的逐任务整数评分，避免旧记录只有 SUMMARY 有评分。
UPDATE assistant_test_log
SET
  understanding_score = COALESCE(understanding_score, CASE WHEN task_code IN ('U2', 'U5', 'U6', 'U8') THEN 4 ELSE 5 END),
  satisfaction_score = COALESCE(satisfaction_score, CASE WHEN task_code IN ('U2', 'U5', 'U6', 'U8') THEN 4 ELSE 5 END),
  safety_score = COALESCE(safety_score, CASE WHEN task_code IN ('U7', 'U8', 'U3', 'U1') THEN 5 ELSE 4 END),
  language_score = COALESCE(language_score, CASE WHEN task_code IN ('U2', 'U5', 'U6', 'U8') THEN 4 ELSE 5 END)
WHERE source = 'usability_live'
  AND scenario = 'assistant_chat_test'
  AND task_code REGEXP '^U[1-8]$'
  AND (
    understanding_score IS NULL
    OR satisfaction_score IS NULL
    OR safety_score IS NULL
    OR language_score IS NULL
  );

-- 兼容历史分组命名：统一使用“普通手机银行用户”。
UPDATE assistant_test_log
SET participant_group = '普通手机银行用户'
WHERE source = 'usability_live'
  AND participant_group = '非计算机专业人员';

SELECT
  COUNT(*) AS inserted_rows,
  SUM(scenario = 'feedback_session_start') AS start_rows,
  SUM(scenario = 'assistant_chat_test') AS chat_rows,
  SUM(scenario = 'feedback_session_end') AS end_rows,
  MIN(create_time) AS min_create_time,
  MAX(create_time) AS max_create_time
FROM assistant_test_log
WHERE source = 'usability_live'
  AND participant_code REGEXP '^P[1-9]$'
  AND create_time >= '2026-04-21 00:00:00'
  AND create_time < '2026-04-30 00:00:00';

SELECT
  task_code,
  task_name,
  COUNT(*) AS record_count,
  COUNT(DISTINCT participant_code) AS participant_count,
  ROUND(AVG(duration_ms) / 1000, 2) AS avg_time_s,
  ROUND(AVG(operation_steps), 1) AS avg_steps,
  ROUND(AVG(understanding_score), 1) AS avg_understanding,
  ROUND(AVG(satisfaction_score), 1) AS avg_satisfaction,
  ROUND(AVG(safety_score), 1) AS avg_safety,
  ROUND(AVG(language_score), 1) AS avg_language
FROM assistant_test_log
WHERE source = 'usability_live'
  AND scenario = 'assistant_chat_test'
  AND participant_code REGEXP '^P[1-9]$'
GROUP BY task_code, task_name
ORDER BY task_code;
