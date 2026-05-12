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

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '16880b34-1876-11f1-9627-2b70b125a5ce:1-222928';

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
INSERT INTO `account` VALUES (1,1,'6222000012345678','储蓄卡',87300.67,'正常','2026-01-11 17:20:48','2026-04-28 16:23:00'),(2,2,'6222000098765432','储蓄卡',11676.50,'正常','2026-01-11 17:25:12','2026-04-28 16:15:04'),(3,3,'6222000043210987','信用卡',12900.00,'正常','2026-01-11 17:30:05','2026-04-28 16:23:00'),(4,3,'6222000076543210','储蓄卡',35273.00,'正常','2026-01-11 17:35:20','2026-04-23 11:06:08');
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assistant_test_log`
--

DROP TABLE IF EXISTS `assistant_test_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assistant_test_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试/运行指标日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID，测试或运行时可为空',
  `source` varchar(30) NOT NULL DEFAULT 'runtime' COMMENT 'runtime、manual、junit等来源',
  `scenario` varchar(80) NOT NULL COMMENT '测试或运行场景，如assistant_chat、intent_test',
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
  `operation_steps` int DEFAULT NULL COMMENT '用户体验测试操作步数',
  `understanding_score` tinyint DEFAULT NULL COMMENT '回答理解度评分，1-5整数',
  `satisfaction_score` tinyint DEFAULT NULL COMMENT '交互满意度评分，1-5整数',
  `safety_score` tinyint DEFAULT NULL COMMENT '安全感评分，1-5整数',
  `language_score` tinyint DEFAULT NULL COMMENT '语言表达评分，1-5整数',
  `user_feedback` varchar(500) DEFAULT NULL COMMENT '匿名测试反馈摘要',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_assistant_test_log_time` (`create_time`),
  KEY `idx_assistant_test_log_source_scenario` (`source`,`scenario`),
  KEY `idx_assistant_test_log_success` (`success`),
  KEY `idx_assistant_test_log_task` (`participant_code`,`task_code`),
  KEY `idx_assistant_test_log_session` (`test_session_id`),
  KEY `idx_assistant_test_log_intent` (`intent`)
) ENGINE=InnoDB AUTO_INCREMENT=1253 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI助手测试与运行指标日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assistant_test_log`
--

LOCK TABLES `assistant_test_log` WRITE;
/*!40000 ALTER TABLE `assistant_test_log` DISABLE KEYS */;
INSERT INTO `assistant_test_log` VALUES (688,1,'usability_live','feedback_session_start','匿名反馈测试开始，角色=普通手机银行用户，说明=本人测试该功能',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260410121409-062a26','ANON-062A26','普通手机银行用户','START','匿名反馈测试开始',1,NULL,NULL,NULL,NULL,NULL,'该次测试不记入数据','2026-04-10 12:14:09'),(689,1,'usability_live','assistant_chat_test','我卡里余额现在是多少','balance','account_info','completed',1,0,1,2442,67,NULL,'TEST-20260410121409-062a26','ANON-062A26','普通手机银行用户','U1','查询账户余额',1,3,5,5,5,5,'该次测试不记入数据','2026-04-10 12:14:14'),(690,1,'usability_live','assistant_chat_test','最近五条花出去的钱列出来','transactions','transaction_query','completed',1,0,1,2577,356,NULL,'TEST-20260410121409-062a26','ANON-062A26','普通手机银行用户','U2','查询最近5条支出流水',1,3,5,5,5,5,'该次测试不记入数据','2026-04-10 12:14:47'),(691,1,'usability_live','assistant_chat_test','银行卡疑似丢失怎么冻结','faq','faq_knowledge_base','recommend',1,0,1,2554,86,NULL,'TEST-20260410121409-062a26','ANON-062A26','普通手机银行用户','U3','银行卡安全FAQ',1,3,5,5,5,5,'该次测试不记入数据','2026-04-10 12:15:00'),(692,1,'usability_live','assistant_chat_test','晚上转账会不会第二天才到','faq','faq_knowledge_base','recommend',1,0,1,2618,81,NULL,'TEST-20260410121409-062a26','ANON-062A26','普通手机银行用户','U4','转账到账FAQ',1,4,5,5,5,5,'该次测试不记入数据','2026-04-10 12:15:13'),(693,1,'usability_live','assistant_chat_test','语音输入：账户里还有多少钱','balance','account_info','completed',1,0,1,2244,67,NULL,'TEST-20260410121409-062a26','ANON-062A26','普通手机银行用户','U5','语音查询余额',1,3,5,5,5,5,'该次测试不记入数据','2026-04-10 12:15:18'),(694,1,'usability_live','assistant_chat_test','本月消费总数和图表给我看看','statistics','statistics_service','completed',1,0,1,6740,213,NULL,'TEST-20260410121409-062a26','ANON-062A26','普通手机银行用户','U6','支出统计图表',1,3,5,5,5,5,'该次测试不记入数据','2026-04-10 12:15:27'),(695,1,'usability_live','assistant_chat_test','给弟弟转300元午餐费','transfer','transfer_validation','awaiting_confirmation',1,0,1,2562,103,NULL,'TEST-20260410121409-062a26','ANON-062A26','普通手机银行用户','U7','转账后取消',1,3,5,5,5,5,'该次测试不记入数据','2026-04-10 12:15:35'),(696,1,'usability_live','assistant_chat_test','取消','transfer','transfer_validation','cancelled',1,0,1,0,11,NULL,'TEST-20260410121409-062a26','ANON-062A26','普通手机银行用户','U7','转账后取消',1,1,5,5,5,5,'该次测试不记入数据','2026-04-10 12:15:36'),(697,1,'usability_live','assistant_chat_test','我现在很生气，想投诉这个服务','emotion_support','rule_emotion','completed',1,0,1,0,98,NULL,'TEST-20260410121409-062a26','ANON-062A26','普通手机银行用户','U8','投诉情绪安抚',1,3,5,5,5,5,'该次测试不记入数据','2026-04-10 12:15:44'),(698,1,'usability_live','feedback_session_end','匿名反馈测试结束并提交评分',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260410121409-062a26','ANON-062A26','普通手机银行用户','SUMMARY','匿名反馈测试评分',1,NULL,5,5,5,5,'该次测试不记入数据','2026-04-10 12:15:55'),(1110,NULL,'usability_live','feedback_session_start','匿名反馈测试开始，角色=计算机专业学生，说明=熟悉系统操作，关注流程是否清晰',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260421124000-p10001','P1','计算机专业学生','START','匿名反馈测试开始',1,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-21 12:40:00'),(1111,NULL,'usability_live','assistant_chat_test','兄弟，帮我瞅一眼卡里还剩多少钱','balance','account_info','completed',1,0,1,2403,71,NULL,'TEST-20260421124000-p10001','P1','计算机专业学生','U1','查询账户余额',1,2,5,5,5,5,'余额结果直给，像平时查卡余额一样顺手。','2026-04-21 12:40:16'),(1112,NULL,'usability_live','assistant_chat_test','把我最近花出去的5笔甩出来','transactions','transaction_query','completed',1,0,1,2408,692,NULL,'TEST-20260421124000-p10001','P1','计算机专业学生','U2','查询最近5条支出流水',1,3,4,4,4,4,'能看懂，但要是默认突出支出会更省脑子。','2026-04-21 12:40:31'),(1113,NULL,'usability_live','assistant_chat_test','卡可能丢了，我先干啥，挂失还是冻结','faq','faq_knowledge_base','recommend',1,0,1,2299,78,NULL,'TEST-20260421124000-p10001','P1','计算机专业学生','U3','银行卡安全FAQ',1,3,4,4,5,4,'FAQ 能先给步骤，再补风险说明，顺序对。','2026-04-21 12:40:46'),(1114,NULL,'usability_live','assistant_chat_test','今晚跨行转账，最晚啥时候到','faq','faq_knowledge_base','recommend',1,0,1,2358,72,NULL,'TEST-20260421124000-p10001','P1','计算机专业学生','U4','转账到账FAQ',1,3,4,4,4,4,'到账时效讲得比较稳，没有乱承诺。','2026-04-21 12:41:01'),(1115,NULL,'usability_live','assistant_chat_test','语音输入：我卡里还剩多少','balance','account_info','completed',1,0,1,2465,68,NULL,'TEST-20260421124000-p10001','P1','计算机专业学生','U5','语音查询余额',1,4,4,4,4,4,'语音转文字后还能命中余额查询。','2026-04-21 12:41:16'),(1116,NULL,'usability_live','assistant_chat_test','本月我花了多少，顺便给个图','statistics','statistics_service','completed',1,0,1,6648,217,NULL,'TEST-20260421124000-p10001','P1','计算机专业学生','U6','支出统计图表',1,3,4,4,4,4,'总额和图能对应上，解释再多一句更好。','2026-04-21 12:41:31'),(1117,NULL,'usability_live','assistant_chat_test','给弟弟转300，备注午饭','transfer','transfer_validation','awaiting_confirmation',1,0,1,2515,104,NULL,'TEST-20260421124000-p10001','P1','计算机专业学生','U7','转账后取消',1,3,5,4,5,4,'先弹确认再转账，这点很稳。','2026-04-21 12:41:46'),(1118,NULL,'usability_live','assistant_chat_test','算了先撤销这笔','transfer','transfer_validation','cancelled',1,0,1,0,18,NULL,'TEST-20260421124000-p10001','P1','计算机专业学生','U7','转账后取消',1,1,5,5,5,5,'取消提示明确，没有真实扣款风险。','2026-04-21 12:42:01'),(1119,NULL,'usability_live','assistant_chat_test','这回答让我有点上头，我要投诉','emotion_support','rule_emotion','completed',1,0,1,0,101,NULL,'TEST-20260421124000-p10001','P1','计算机专业学生','U8','投诉情绪安抚',1,3,4,4,5,4,'先安抚再引导人工，节奏没问题。','2026-04-21 12:42:16'),(1120,NULL,'usability_live','feedback_session_end','匿名反馈测试结束并提交评分',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260421124000-p10001','P1','计算机专业学生','SUMMARY','匿名反馈测试评分',1,NULL,5,5,5,5,'舍友反馈：流程顺，尤其是转账前确认和取消，心里有底。','2026-04-21 12:43:30'),(1121,NULL,'usability_live','feedback_session_start','匿名反馈测试开始，角色=计算机专业学生，说明=关注AI处理过程和页面反馈',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260422165000-p20002','P2','计算机专业学生','START','匿名反馈测试开始',1,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-22 16:50:00'),(1122,NULL,'usability_live','assistant_chat_test','查个余额，看看今天还能不能点奶茶','balance','account_info','completed',1,0,1,2506,72,NULL,'TEST-20260422165000-p20002','P2','计算机专业学生','U1','查询账户余额',1,2,5,4,5,4,'入口好找，普通口语也能识别。','2026-04-22 16:50:17'),(1123,NULL,'usability_live','assistant_chat_test','近5条消费流水拉一下','transactions','transaction_query','completed',1,0,1,2459,700,NULL,'TEST-20260422165000-p20002','P2','计算机专业学生','U2','查询最近5条支出流水',1,3,4,4,4,4,'返回是支出方向，格式清楚。','2026-04-22 16:50:32'),(1124,NULL,'usability_live','assistant_chat_test','要是卡被捡走了，我是不是先挂失','faq','faq_knowledge_base','completed',1,0,1,2363,76,NULL,'TEST-20260422165000-p20002','P2','计算机专业学生','U3','银行卡安全FAQ',1,3,5,4,5,4,'丢卡建议覆盖到位，行动顺序清晰。','2026-04-22 16:50:47'),(1125,NULL,'usability_live','assistant_chat_test','晚上转账会不会拖到明天','faq','faq_knowledge_base','completed',1,0,1,2422,70,NULL,'TEST-20260422165000-p20002','P2','计算机专业学生','U4','转账到账FAQ',1,3,4,4,4,4,'非工作时段的提示有提到，不会误导。','2026-04-22 16:51:02'),(1126,NULL,'usability_live','assistant_chat_test','语音输入：查下我账户余额','balance','account_info','completed',1,0,1,2555,68,NULL,'TEST-20260422165000-p20002','P2','计算机专业学生','U5','语音查询余额',1,4,4,4,4,4,'语音识别后直接走余额链路，少了一步。','2026-04-22 16:51:17'),(1127,NULL,'usability_live','assistant_chat_test','算下这个月支出总额','statistics','statistics_service','completed',1,0,1,6686,224,NULL,'TEST-20260422165000-p20002','P2','计算机专业学生','U6','支出统计图表',1,3,4,4,4,4,'统计口径说明能看懂。','2026-04-22 16:51:32'),(1128,NULL,'usability_live','assistant_chat_test','给我弟打300午饭钱','transfer','transfer_validation','awaiting_confirmation',1,0,1,2579,113,NULL,'TEST-20260422165000-p20002','P2','计算机专业学生','U7','转账后取消',1,3,4,4,5,4,'待确认状态设计合理，能防手滑。','2026-04-22 16:51:47'),(1129,NULL,'usability_live','assistant_chat_test','取消转账','transfer','transfer_validation','cancelled',1,0,1,0,15,NULL,'TEST-20260422165000-p20002','P2','计算机专业学生','U7','转账后取消',1,1,5,4,5,4,'取消后状态清空，交互预期一致。','2026-04-22 16:52:02'),(1130,NULL,'usability_live','assistant_chat_test','我挺不爽的，想投诉一下','emotion_support','rule_emotion','completed',1,0,1,0,98,NULL,'TEST-20260422165000-p20002','P2','计算机专业学生','U8','投诉情绪安抚',1,3,4,4,5,4,'负面情绪能被识别并给出转人工。','2026-04-22 16:52:17'),(1131,NULL,'usability_live','feedback_session_end','匿名反馈测试结束并提交评分',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260422165000-p20002','P2','计算机专业学生','SUMMARY','匿名反馈测试评分',1,NULL,5,4,5,4,'舍友反馈：口语输入基本都能命中，统计解释还能再短一点。','2026-04-22 16:53:30'),(1132,NULL,'usability_live','feedback_session_start','匿名反馈测试开始，角色=计算机专业学生，说明=关注自然语言表达是否容易命中',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260423210500-p30003','P3','计算机专业学生','START','匿名反馈测试开始',1,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-23 21:05:00'),(1133,NULL,'usability_live','assistant_chat_test','看下我卡余额还剩多少','balance','account_info','completed',1,0,1,2649,77,NULL,'TEST-20260423210500-p30003','P3','计算机专业学生','U1','查询账户余额',1,2,5,5,5,5,'答案干净，不啰嗦。','2026-04-23 21:05:18'),(1134,NULL,'usability_live','assistant_chat_test','最近5笔支出明细整一个','transactions','transaction_query','completed',1,0,1,2602,690,NULL,'TEST-20260423210500-p30003','P3','计算机专业学生','U2','查询最近5条支出流水',1,3,4,4,4,4,'时间和金额都给了，够用。','2026-04-23 21:05:33'),(1135,NULL,'usability_live','assistant_chat_test','手机上能临时冻结银行卡不','faq','faq_knowledge_base','completed',1,0,1,2545,73,NULL,'TEST-20260423210500-p30003','P3','计算机专业学生','U3','银行卡安全FAQ',1,3,4,4,5,4,'冻结和挂失的区别讲得明白。','2026-04-23 21:05:48'),(1136,NULL,'usability_live','assistant_chat_test','普通转账多久到账啊','faq','faq_knowledge_base','completed',1,0,1,2526,79,NULL,'TEST-20260423210500-p30003','P3','计算机专业学生','U4','转账到账FAQ',1,3,4,4,4,4,'有不确定性提示，回答比较稳。','2026-04-23 21:06:03'),(1137,NULL,'usability_live','assistant_chat_test','语音输入：查当前账户余额','balance','account_info','completed',1,0,1,2685,68,NULL,'TEST-20260423210500-p30003','P3','计算机专业学生','U5','语音查询余额',1,3,4,4,4,4,'语音入口响应快，基本无学习成本。','2026-04-23 21:06:18'),(1138,NULL,'usability_live','assistant_chat_test','把本月消费总数和图给我','statistics','statistics_service','completed',1,0,1,6842,215,NULL,'TEST-20260423210500-p30003','P3','计算机专业学生','U6','支出统计图表',1,3,4,4,4,4,'图表直观，数字口径一致。','2026-04-23 21:06:33'),(1139,NULL,'usability_live','assistant_chat_test','转300给弟弟，备注午餐费','transfer','transfer_validation','awaiting_confirmation',1,0,1,2748,105,NULL,'TEST-20260423210500-p30003','P3','计算机专业学生','U7','转账后取消',1,3,5,4,5,4,'二次确认做得好，误转风险低。','2026-04-23 21:06:48'),(1140,NULL,'usability_live','assistant_chat_test','不用转了，取消','transfer','transfer_validation','cancelled',1,0,1,40,18,NULL,'TEST-20260423210500-p30003','P3','计算机专业学生','U7','转账后取消',1,1,5,5,5,5,'撤销结果明确，流程闭环。','2026-04-23 21:07:03'),(1141,NULL,'usability_live','assistant_chat_test','我有点急，想走投诉渠道','emotion_support','rule_emotion','completed',1,0,1,92,98,NULL,'TEST-20260423210500-p30003','P3','计算机专业学生','U8','投诉情绪安抚',1,3,4,4,5,4,'安抚语气和人工入口衔接可以。','2026-04-23 21:07:18'),(1142,NULL,'usability_live','feedback_session_end','匿名反馈测试结束并提交评分',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260423210500-p30003','P3','计算机专业学生','SUMMARY','匿名反馈测试评分',1,NULL,4,5,5,4,'舍友反馈：查询与投诉引导都在线，语音入口也比较好找。','2026-04-23 21:08:30'),(1143,NULL,'usability_live','feedback_session_start','匿名反馈测试开始，角色=普通手机银行用户，说明=经常使用手机银行，表达偏口语化',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260424093000-p40004','P4','普通手机银行用户','START','匿名反馈测试开始',1,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-24 09:30:00'),(1144,NULL,'usability_live','assistant_chat_test','我想看看账户里钱还够不够','balance','account_info','completed',1,0,1,2895,68,NULL,'TEST-20260424093000-p40004','P4','普通手机银行用户','U1','查询账户余额',1,3,4,4,4,4,'能查到余额，但我得看两遍才反应过来。','2026-04-24 09:30:19'),(1145,NULL,'usability_live','assistant_chat_test','最近花的钱大概啥情况，给我看下','transactions','transaction_query','completed',1,0,1,2783,694,NULL,'TEST-20260424093000-p40004','P4','普通手机银行用户','U2','查询最近5条支出流水',1,4,3,3,4,3,'我说得比较模糊，系统还能给出结果。','2026-04-24 09:30:34'),(1146,NULL,'usability_live','assistant_chat_test','银行卡找不到了我该先做哪一步','faq','faq_knowledge_base','recommend',1,0,1,2700,75,NULL,'TEST-20260424093000-p40004','P4','普通手机银行用户','U3','银行卡安全FAQ',1,3,4,4,5,4,'先挂失再处理的提示让我安心。','2026-04-24 09:30:49'),(1147,NULL,'usability_live','assistant_chat_test','跨行转账一般什么时候能到呀','faq','faq_knowledge_base','completed',1,0,1,2798,72,NULL,'TEST-20260424093000-p40004','P4','普通手机银行用户','U4','转账到账FAQ',1,3,4,4,4,4,'时间说法比较日常，我能理解。','2026-04-24 09:31:04'),(1148,NULL,'usability_live','assistant_chat_test','语音输入：我这个账户还有钱吗','balance','account_info','completed',1,0,1,2840,70,NULL,'TEST-20260424093000-p40004','P4','普通手机银行用户','U5','语音查询余额',1,5,3,3,3,3,'语音能用，但有时要重新说一遍。','2026-04-24 09:31:19'),(1149,NULL,'usability_live','assistant_chat_test','这个月总共花了多少钱，图也看一下','statistics','statistics_service','completed',1,0,1,7036,220,NULL,'TEST-20260424093000-p40004','P4','普通手机银行用户','U6','支出统计图表',1,4,3,3,4,3,'图表有帮助，不过解释再简单点更好。','2026-04-24 09:31:34'),(1150,NULL,'usability_live','assistant_chat_test','给弟弟转三百块午饭钱','transfer','transfer_validation','awaiting_confirmation',1,0,1,2929,113,NULL,'TEST-20260424093000-p40004','P4','普通手机银行用户','U7','转账后取消',1,3,4,4,5,4,'先确认再执行，这一步很必要。','2026-04-24 09:31:49'),(1151,NULL,'usability_live','assistant_chat_test','先别转了，取消掉','transfer','transfer_validation','cancelled',1,0,1,273,19,NULL,'TEST-20260424093000-p40004','P4','普通手机银行用户','U7','转账后取消',1,2,4,4,5,4,'取消后我比较放心。','2026-04-24 09:32:04'),(1152,NULL,'usability_live','assistant_chat_test','我有点生气，想找人工客服','emotion_support','rule_emotion','completed',1,0,1,325,99,NULL,'TEST-20260424093000-p40004','P4','普通手机银行用户','U8','投诉情绪安抚',1,3,4,4,5,4,'情绪场景有安抚，不会继续答非所问。','2026-04-24 09:32:19'),(1153,NULL,'usability_live','feedback_session_end','匿名反馈测试结束并提交评分',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260424093000-p40004','P4','普通手机银行用户','SUMMARY','匿名反馈测试评分',1,NULL,4,4,4,4,'普通用户反馈：能完成主要操作，但模糊问法下还想要更直白提示。','2026-04-24 09:33:30'),(1154,NULL,'usability_live','feedback_session_start','匿名反馈测试开始，角色=普通手机银行用户，说明=首次体验语音输入和图表统计',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260425153000-p50005','P5','普通手机银行用户','START','匿名反馈测试开始',1,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-25 15:30:00'),(1155,NULL,'usability_live','assistant_chat_test','帮我瞧瞧现在余额还有多少','balance','account_info','completed',1,0,1,3055,68,NULL,'TEST-20260425153000-p50005','P5','普通手机银行用户','U1','查询账户余额',1,3,4,4,4,4,'余额回复清楚，数字看得懂。','2026-04-25 15:30:20'),(1156,NULL,'usability_live','assistant_chat_test','查最近五笔花销记录吧','transactions','transaction_query','completed',1,0,1,2969,700,NULL,'TEST-20260425153000-p50005','P5','普通手机银行用户','U2','查询最近5条支出流水',1,4,4,3,4,3,'能看懂消费记录，但筛选词我不太会说。','2026-04-25 15:30:35'),(1157,NULL,'usability_live','assistant_chat_test','卡不见了，是先挂失还是直接补卡','faq','faq_knowledge_base','completed',1,0,1,2873,76,NULL,'TEST-20260425153000-p50005','P5','普通手机银行用户','U3','银行卡安全FAQ',1,3,4,4,5,4,'FAQ 先后步骤比较清楚。','2026-04-25 15:30:50'),(1158,NULL,'usability_live','assistant_chat_test','我转出去的钱一般多久能到','faq','faq_knowledge_base','recommend',1,0,1,2945,71,NULL,'TEST-20260425153000-p50005','P5','普通手机银行用户','U4','转账到账FAQ',1,3,4,4,4,4,'到账说明不夸大，感觉靠谱。','2026-04-25 15:31:05'),(1159,NULL,'usability_live','assistant_chat_test','语音输入：看下我卡里余额','balance','account_info','completed',1,0,1,3065,68,NULL,'TEST-20260425153000-p50005','P5','普通手机银行用户','U5','语音查询余额',1,5,3,3,3,3,'语音权限弹窗让我停了一下。','2026-04-25 15:31:20'),(1160,NULL,'usability_live','assistant_chat_test','本月支出总数给我算一下，再看看图','statistics','statistics_service','completed',1,0,1,7196,220,NULL,'TEST-20260425153000-p50005','P5','普通手机银行用户','U6','支出统计图表',1,4,3,3,4,3,'图能看，但我更依赖文字总结。','2026-04-25 15:31:35'),(1161,NULL,'usability_live','assistant_chat_test','给弟弟转300元，备注午餐','transfer','transfer_validation','awaiting_confirmation',1,0,1,3128,105,NULL,'TEST-20260425153000-p50005','P5','普通手机银行用户','U7','转账后取消',1,3,4,4,5,4,'确认页能避免我手误。','2026-04-25 15:31:50'),(1162,NULL,'usability_live','assistant_chat_test','取消这笔','transfer','transfer_validation','cancelled',1,0,1,472,15,NULL,'TEST-20260425153000-p50005','P5','普通手机银行用户','U7','转账后取消',1,2,4,4,5,4,'取消动作反馈及时。','2026-04-25 15:32:05'),(1163,NULL,'usability_live','assistant_chat_test','这次体验不太满意，我要投诉','emotion_support','rule_emotion','completed',1,0,1,498,100,NULL,'TEST-20260425153000-p50005','P5','普通手机银行用户','U8','投诉情绪安抚',1,3,4,4,5,4,'投诉场景没有跑偏到别的话题。','2026-04-25 15:32:20'),(1164,NULL,'usability_live','feedback_session_end','匿名反馈测试结束并提交评分',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260425153000-p50005','P5','普通手机银行用户','SUMMARY','匿名反馈测试评分',1,NULL,4,3,4,4,'普通用户反馈：语音权限和图表理解有一点门槛，文字总结更关键。','2026-04-25 15:33:30'),(1165,NULL,'usability_live','feedback_session_start','匿名反馈测试开始，角色=普通手机银行用户，说明=环境噪声较明显，语音任务需二次修改',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260426201000-p60006','P6','普通手机银行用户','START','匿名反馈测试开始',1,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-26 20:10:00'),(1166,NULL,'usability_live','assistant_chat_test','帮我看下银行卡里还剩多少钱','balance','account_info','completed',1,0,1,3158,69,NULL,'TEST-20260426201000-p60006','P6','普通手机银行用户','U1','查询账户余额',1,3,4,4,4,3,'余额能查到，流程还算顺。','2026-04-26 20:10:21'),(1167,NULL,'usability_live','assistant_chat_test','最近5条支出明细有吗，大概看看','transactions','transaction_query','completed',1,0,1,3033,694,NULL,'TEST-20260426201000-p60006','P6','普通手机银行用户','U2','查询最近5条支出流水',1,4,3,3,4,3,'我表述不精确也能返回结果。','2026-04-26 20:10:36'),(1168,NULL,'usability_live','assistant_chat_test','银行卡疑似丢了怎么先冻结一下','faq','faq_knowledge_base','recommend',1,0,1,2950,75,NULL,'TEST-20260426201000-p60006','P6','普通手机银行用户','U3','银行卡安全FAQ',1,3,4,4,5,4,'风险提示和处理步骤都有提到。','2026-04-26 20:10:51'),(1169,NULL,'usability_live','assistant_chat_test','节假日转账会不会慢一点到账','faq','faq_knowledge_base','completed',1,0,1,3048,72,NULL,'TEST-20260426201000-p60006','P6','普通手机银行用户','U4','转账到账FAQ',1,3,4,4,4,4,'延迟风险说明比较清楚。','2026-04-26 20:11:06'),(1170,NULL,'usability_live','assistant_chat_test','语音输入：帮我查余额','balance','account_info','completed',1,0,1,3129,77,NULL,'TEST-20260426201000-p60006','P6','普通手机银行用户','U5','语音查询余额',1,5,3,3,3,3,'环境有噪声时识别会偏，需要改一次字。','2026-04-26 20:11:21'),(1171,NULL,'usability_live','assistant_chat_test','本月花费统计和图表一起给我','statistics','statistics_service','completed',1,0,1,7338,217,NULL,'TEST-20260426201000-p60006','P6','普通手机银行用户','U6','支出统计图表',1,4,3,3,4,3,'图表完整，但我理解还需要时间。','2026-04-26 20:11:36'),(1172,NULL,'usability_live','assistant_chat_test','给弟弟转三百块，写午餐费','transfer','transfer_validation','awaiting_confirmation',1,0,1,3205,104,NULL,'TEST-20260426201000-p60006','P6','普通手机银行用户','U7','转账后取消',1,3,4,4,5,4,'确认页防误转作用明显。','2026-04-26 20:11:51'),(1173,NULL,'usability_live','assistant_chat_test','取消刚才那笔转账','transfer','transfer_validation','cancelled',1,0,1,523,19,NULL,'TEST-20260426201000-p60006','P6','普通手机银行用户','U7','转账后取消',1,2,4,4,5,4,'撤销反馈明确。','2026-04-26 20:12:06'),(1174,NULL,'usability_live','assistant_chat_test','我现在挺烦的，转人工投诉吧','emotion_support','rule_emotion','completed',1,0,1,588,100,NULL,'TEST-20260426201000-p60006','P6','普通手机银行用户','U8','投诉情绪安抚',1,3,4,4,5,4,'情绪安抚后能直接给人工入口。','2026-04-26 20:12:21'),(1175,NULL,'usability_live','feedback_session_end','匿名反馈测试结束并提交评分',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260426201000-p60006','P6','普通手机银行用户','SUMMARY','匿名反馈测试评分',1,NULL,3,3,4,3,'普通用户反馈：噪声环境下语音容易偏，其他流程基本可完成。','2026-04-26 20:13:30'),(1176,NULL,'usability_live','feedback_session_start','匿名反馈测试开始，角色=计算机行业开发者，说明=关注接口状态、日志和异常提示',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260427084500-p70007','P7','计算机行业开发者','START','匿名反馈测试开始',1,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-27 08:45:00'),(1177,NULL,'usability_live','assistant_chat_test','查询 demo_account 可用余额，返回金额与更新时间','balance','account_info','completed',1,0,1,2466,75,NULL,'TEST-20260427084500-p70007','P7','计算机行业开发者','U1','查询账户余额',1,2,5,4,5,4,'返回字段稳定，余额来源可追溯。','2026-04-27 08:45:22'),(1178,NULL,'usability_live','assistant_chat_test','按 direction=OUT 查询最近5条交易，按时间倒序','transactions','transaction_query','completed',1,0,1,2406,698,NULL,'TEST-20260427084500-p70007','P7','计算机行业开发者','U2','查询最近5条支出流水',1,3,5,4,4,4,'排序和方向过滤符合预期。','2026-04-27 08:45:37'),(1179,NULL,'usability_live','assistant_chat_test','银行卡遗失场景，请返回挂失与冻结的处理顺序','faq','faq_knowledge_base','completed',1,0,1,2310,82,NULL,'TEST-20260427084500-p70007','P7','计算机行业开发者','U3','银行卡安全FAQ',1,3,4,4,5,4,'知识库命中准确，动作顺序可执行。','2026-04-27 08:45:52'),(1180,NULL,'usability_live','assistant_chat_test','说明跨行转账 T+0/T+1 到账条件','faq','faq_knowledge_base','completed',1,0,1,2395,78,NULL,'TEST-20260427084500-p70007','P7','计算机行业开发者','U4','转账到账FAQ',1,3,4,4,4,4,'FAQ 对边界条件描述清楚。','2026-04-27 08:46:07'),(1181,NULL,'usability_live','assistant_chat_test','语音输入：查询当前可用余额','balance','account_info','completed',1,0,1,2528,69,NULL,'TEST-20260427084500-p70007','P7','计算机行业开发者','U5','语音查询余额',1,3,4,4,4,4,'语音只影响输入层，后端链路一致。','2026-04-27 08:46:22'),(1182,NULL,'usability_live','assistant_chat_test','统计本月支出并返回图表数据与口径','statistics','statistics_service','completed',1,0,1,6646,220,NULL,'TEST-20260427084500-p70007','P7','计算机行业开发者','U6','支出统计图表',1,3,5,4,4,4,'统计数值与图表一致，可复核。','2026-04-27 08:46:37'),(1183,NULL,'usability_live','assistant_chat_test','发起向弟弟转账300元，验证待确认状态','transfer','transfer_validation','awaiting_confirmation',1,0,1,2565,111,NULL,'TEST-20260427084500-p70007','P7','计算机行业开发者','U7','转账后取消',1,3,5,4,5,4,'transfer_validation 状态流转正确。','2026-04-27 08:46:52'),(1184,NULL,'usability_live','assistant_chat_test','取消待确认转账','transfer','transfer_validation','cancelled',1,0,1,0,18,NULL,'TEST-20260427084500-p70007','P7','计算机行业开发者','U7','转账后取消',1,1,5,4,5,4,'取消后 pending 状态清理正确。','2026-04-27 08:47:07'),(1185,NULL,'usability_live','assistant_chat_test','模拟投诉：回答不满意，请转人工','emotion_support','rule_emotion','completed',1,0,1,0,102,NULL,'TEST-20260427084500-p70007','P7','计算机行业开发者','U8','投诉情绪安抚',1,3,4,4,5,4,'emotion_support 分流规则可预期。','2026-04-27 08:47:22'),(1186,NULL,'usability_live','feedback_session_end','匿名反馈测试结束并提交评分',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260427084500-p70007','P7','计算机行业开发者','SUMMARY','匿名反馈测试评分',1,NULL,5,4,5,5,'开发者反馈：状态流转、日志字段和取消链路清晰，可用于排查。','2026-04-27 08:48:30'),(1187,NULL,'usability_live','feedback_session_start','匿名反馈测试开始，角色=计算机行业开发者，说明=关注转账确认和统计链路',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260428112000-p80008','P8','计算机行业开发者','START','匿名反馈测试开始',1,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-28 11:20:00'),(1188,NULL,'usability_live','assistant_chat_test','查询账户可用余额，并标识数据来源模块','balance','account_info','completed',1,0,1,2582,74,NULL,'TEST-20260428112000-p80008','P8','计算机行业开发者','U1','查询账户余额',1,2,5,4,5,4,'account_info 调用路径清晰。','2026-04-28 11:20:23'),(1189,NULL,'usability_live','assistant_chat_test','获取最近5条支出交易，返回金额和交易时间','transactions','transaction_query','completed',1,0,1,2548,699,NULL,'TEST-20260428112000-p80008','P8','计算机行业开发者','U2','查询最近5条支出流水',1,3,4,4,4,4,'transaction_query 输出结构完整。','2026-04-28 11:20:38'),(1190,NULL,'usability_live','assistant_chat_test','卡遗失后手机银行挂失流程给出标准步骤','faq','faq_knowledge_base','completed',1,0,1,2452,79,NULL,'TEST-20260428112000-p80008','P8','计算机行业开发者','U3','银行卡安全FAQ',1,3,4,4,5,4,'FAQ 返回具备可操作性。','2026-04-28 11:20:53'),(1191,NULL,'usability_live','assistant_chat_test','行内与跨行转账到账时效是否一致，请区分说明','faq','faq_knowledge_base','completed',1,0,1,2420,80,NULL,'TEST-20260428112000-p80008','P8','计算机行业开发者','U4','转账到账FAQ',1,3,4,4,4,4,'时效差异和不确定性说明到位。','2026-04-28 11:21:08'),(1192,NULL,'usability_live','assistant_chat_test','语音输入：查询账户余额','balance','account_info','completed',1,0,1,2592,67,NULL,'TEST-20260428112000-p80008','P8','计算机行业开发者','U5','语音查询余额',1,3,4,4,4,4,'语音入口与文本链路一致。','2026-04-28 11:21:23'),(1193,NULL,'usability_live','assistant_chat_test','汇总本月支出并展示图表','statistics','statistics_service','completed',1,0,1,6762,215,NULL,'TEST-20260428112000-p80008','P8','计算机行业开发者','U6','支出统计图表',1,3,4,4,4,4,'统计口径可解释，图表无冲突。','2026-04-28 11:21:38'),(1194,NULL,'usability_live','assistant_chat_test','给弟弟转300元并附言午餐，检查确认页','transfer','transfer_validation','awaiting_confirmation',1,0,1,2655,111,NULL,'TEST-20260428112000-p80008','P8','计算机行业开发者','U7','转账后取消',1,3,5,4,5,4,'二次确认信息完整。','2026-04-28 11:21:53'),(1195,NULL,'usability_live','assistant_chat_test','取消该转账请求','transfer','transfer_validation','cancelled',1,0,1,0,18,NULL,'TEST-20260428112000-p80008','P8','计算机行业开发者','U7','转账后取消',1,1,5,4,5,4,'取消反馈及时且状态正确。','2026-04-28 11:22:08'),(1196,NULL,'usability_live','assistant_chat_test','我要投诉当前回复体验，请提供人工入口','emotion_support','rule_emotion','completed',1,0,1,12,105,NULL,'TEST-20260428112000-p80008','P8','计算机行业开发者','U8','投诉情绪安抚',1,3,4,4,5,4,'负面情绪识别正常，转人工路径明确。','2026-04-28 11:22:23'),(1197,NULL,'usability_live','feedback_session_end','匿名反馈测试结束并提交评分',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260428112000-p80008','P8','计算机行业开发者','SUMMARY','匿名反馈测试评分',1,NULL,4,4,5,4,'开发者反馈：查询与转账确认链路稳定，FAQ 边界表达合理。','2026-04-28 11:23:30'),(1198,NULL,'usability_live','feedback_session_start','匿名反馈测试开始，角色=银行业务/企业导师，说明=从业务边界和论文实验设计角度观察',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260429101000-p90009','P9','银行业务/企业导师','START','匿名反馈测试开始',1,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-29 10:10:00'),(1199,NULL,'usability_live','assistant_chat_test','请在演示数据范围内返回当前可用余额，并注明数据来源口径','balance','account_info','completed',1,0,1,2768,72,NULL,'TEST-20260429101000-p90009','P9','银行业务/企业导师','U1','查询账户余额',1,2,5,5,5,4,'余额结果与演示数据边界说明一致。','2026-04-29 10:10:24'),(1200,NULL,'usability_live','assistant_chat_test','请输出最近5笔支出流水，至少包含时间戳、金额与摘要字段','transactions','transaction_query','completed',1,0,1,2708,695,NULL,'TEST-20260429101000-p90009','P9','银行业务/企业导师','U2','查询最近5条支出流水',1,3,5,4,4,4,'流水字段满足评审复核要求。','2026-04-29 10:10:39'),(1201,NULL,'usability_live','assistant_chat_test','银行卡遗失场景下，请给出推荐动作顺序及风险提示','faq','faq_knowledge_base','completed',1,0,1,2586,73,NULL,'TEST-20260429101000-p90009','P9','银行业务/企业导师','U3','银行卡安全FAQ',1,3,5,4,5,4,'处理顺序和风险提示完整，符合业务预期。','2026-04-29 10:10:54'),(1202,NULL,'usability_live','assistant_chat_test','请说明行内/跨行转账到账时效及不确定性边界','faq','faq_knowledge_base','completed',1,0,1,2580,80,NULL,'TEST-20260429101000-p90009','P9','银行业务/企业导师','U4','转账到账FAQ',1,3,4,4,4,4,'未做绝对承诺，表达符合客服审慎口径。','2026-04-29 10:11:09'),(1203,NULL,'usability_live','assistant_chat_test','语音输入：请查询演示账户余额并标注口径','balance','account_info','completed',1,0,1,2765,75,NULL,'TEST-20260429101000-p90009','P9','银行业务/企业导师','U5','语音查询余额',1,3,4,4,4,4,'语音作为输入增强可用，但仍需用户确认文本。','2026-04-29 10:11:24'),(1204,NULL,'usability_live','assistant_chat_test','请展示本月支出总额、统计口径与图表说明','statistics','statistics_service','completed',1,0,1,6935,223,NULL,'TEST-20260429101000-p90009','P9','银行业务/企业导师','U6','支出统计图表',1,3,4,4,4,4,'统计结果可复算，图表表达一致。','2026-04-29 10:11:39'),(1205,NULL,'usability_live','assistant_chat_test','发起向弟弟转300元流程校验，需二次确认','transfer','transfer_validation','awaiting_confirmation',1,0,1,2828,112,NULL,'TEST-20260429101000-p90009','P9','银行业务/企业导师','U7','转账后取消',1,3,5,4,5,4,'二次确认机制满足安全控制要求。','2026-04-29 10:11:54'),(1206,NULL,'usability_live','assistant_chat_test','取消上一笔待确认转账','transfer','transfer_validation','cancelled',1,0,1,159,21,NULL,'TEST-20260429101000-p90009','P9','银行业务/企业导师','U7','转账后取消',1,1,5,5,5,4,'取消动作闭环完整，无误执行。','2026-04-29 10:12:09'),(1207,NULL,'usability_live','assistant_chat_test','用户情绪激动时，请执行转人工分流并保留安抚语','emotion_support','rule_emotion','completed',1,0,1,133,98,NULL,'TEST-20260429101000-p90009','P9','银行业务/企业导师','U8','投诉情绪安抚',1,3,5,4,5,4,'情绪分流策略清晰，符合服务规范。','2026-04-29 10:12:24'),(1208,NULL,'usability_live','feedback_session_end','匿名反馈测试结束并提交评分',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260429101000-p90009','P9','银行业务/企业导师','SUMMARY','匿名反馈测试评分',1,NULL,5,4,5,4,'行业专家反馈：业务边界、风险提示与论文实验口径一致，建议继续强化口径说明。','2026-04-29 10:13:30'),(1237,1,'runtime','assistant_chat','帮我瞅一眼余额还有多少',NULL,NULL,NULL,NULL,NULL,0,407,NULL,'Connection reset',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-30 14:31:06'),(1238,1,'runtime','assistant_chat','帮我瞅瞅余额还剩多少','balance','account_info','completed',1,0,1,2767,67,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-30 14:31:30'),(1239,1,'runtime','assistant_chat','把我最近花出去的5笔甩出来','transactions','transaction_query','completed',1,0,1,2858,356,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-30 14:31:59'),(1240,1,'runtime','assistant_chat','卡可能丢了，我先干啥，挂失还是冻结','faq','faq_knowledge_base','recommend',1,0,1,3191,86,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-30 14:32:07'),(1241,1,'runtime','assistant_chat','今晚跨行转账，最晚啥时候到','faq','faq_knowledge_base','recommend',1,0,1,2459,81,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-30 14:32:29'),(1242,1,'runtime','assistant_chat','转账多久到账？','faq','faq_knowledge_base','completed',1,0,1,2414,75,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-30 14:33:00'),(1243,1,'runtime','assistant_chat','我挺不爽的，想投诉一下','other','qwen_fallback','completed',1,0,1,4733,72,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-30 14:35:04'),(1244,1,'runtime','assistant_chat','我要投诉！','emotion_support','rule_emotion','completed',1,0,1,0,108,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-30 14:35:18'),(1245,1,'runtime','assistant_chat','银行卡找不到了我该先做哪一步','faq','faq_knowledge_base','recommend',1,0,1,2881,86,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-30 14:36:05'),(1246,1,'runtime','assistant_chat','银行卡丢失后如何处理？','faq','faq_knowledge_base','completed',1,0,1,2406,66,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-30 14:36:13'),(1247,1,'runtime','assistant_chat','看一下今年每个月的支出趋势','statistics','statistics_service','completed',1,0,1,7347,235,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-06 11:42:28'),(1248,1,'runtime','assistant_chat','统计本月总支出','statistics','statistics_service','completed',1,0,1,7923,282,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-06 11:58:37'),(1249,1,'runtime','assistant_chat','统计下本月流水','statistics','statistics_service','completed',1,0,1,9300,370,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-06 12:00:52'),(1250,1,'runtime','assistant_chat','给弟弟转300元午餐费','transfer','transfer_validation','awaiting_confirmation',1,0,1,2721,103,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-06 12:02:18'),(1251,1,'usability_live','feedback_session_start','匿名反馈测试开始，角色=普通手机银行用户，说明=111',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260506144202-134a90','ANON-134A90','普通手机银行用户','START','匿名反馈测试开始',1,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-06 14:42:02'),(1252,1,'usability_live','feedback_session_end','匿名反馈测试结束并提交评分',NULL,NULL,NULL,1,0,1,NULL,NULL,NULL,'TEST-20260506144202-134a90','ANON-134A90','普通手机银行用户','SUMMARY','匿名反馈测试评分',1,NULL,5,5,5,5,'不记录','2026-05-06 14:47:25');
/*!40000 ALTER TABLE `assistant_test_log` ENABLE KEYS */;
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
-- Table structure for table `faq_knowledge`
--

DROP TABLE IF EXISTS `faq_knowledge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `faq_knowledge` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识库ID',
  `category` varchar(50) NOT NULL COMMENT '问题分类，如账户、转账、贷款、安全、网银',
  `standard_question` varchar(255) NOT NULL COMMENT '标准问题',
  `aliases` text COMMENT '相似问法，多个问法用分号分隔',
  `keywords` varchar(255) DEFAULT NULL COMMENT '关键词，多个关键词用分号分隔',
  `answer` text NOT NULL COMMENT '标准答案',
  `status` tinyint DEFAULT '1' COMMENT '1启用，0停用',
  `priority` int DEFAULT '0' COMMENT '优先级',
  `source` varchar(255) DEFAULT NULL COMMENT '答案来源或维护说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_faq_status_priority` (`status`,`priority`),
  KEY `idx_faq_category` (`category`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='FAQ知识库表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `faq_knowledge`
--

LOCK TABLES `faq_knowledge` WRITE;
/*!40000 ALTER TABLE `faq_knowledge` DISABLE KEYS */;
INSERT INTO `faq_knowledge` VALUES (1,'账户业务','如何查询账户余额？','怎么查余额；我想看账户余额；银行卡还有多少钱','余额；账户余额；查询余额','你可以在系统首页或账户总览页面查看当前账户余额，也可以直接问“查一下我的余额”，系统会返回已登录账户的实时余额。',1,100,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(2,'账户业务','如何修改银行预留手机号？','我想改手机号；绑定手机号怎么换；预留手机号不用了怎么办','手机号；预留手机；修改手机号','修改银行预留手机号通常需要完成身份验证。你可以先在手机银行的个人资料或安全设置中尝试修改；如果无法线上完成，请携带有效身份证件和银行卡到网点办理。',1,90,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(3,'账户业务','如何更新身份证件信息？','身份证过期了怎么办；证件信息怎么改；身份证有效期怎么更新','身份证；证件；更新证件','身份证件过期或信息变化时，请尽快通过手机银行证件更新入口或银行网点更新资料。办理时通常需要本人有效身份证件。',1,80,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(4,'账户业务','如何查询开户行？','开户网点怎么查；我这张卡是哪家支行；开户行信息在哪里看','开户行；开户网点；支行','你可以在账户详情、电子账户信息或手机银行的银行卡管理页面查询开户行。若页面未展示，可联系人工客服核验后查询。',1,80,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(5,'账户业务','账户被冻结怎么办？','银行卡被冻结了；账户不能用了怎么办；卡被限制交易','冻结；限制交易；账户异常','账户冻结可能与司法、风险控制、身份信息异常等原因有关。请先查看系统提示原因，并携带身份证件和银行卡到网点或联系人工客服核实处理。',1,95,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(6,'银行卡业务','银行卡丢失后如何处理？','银行卡丢失怎么办；银行卡掉了怎么办；卡丢了怎么挂失；银行卡不见了会不会被盗刷','银行卡；丢失；挂失','银行卡丢失后请立即办理挂失，避免资金风险。你可以通过手机银行、客服电话或银行网点办理临时/正式挂失，随后按要求补办新卡。',1,120,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(7,'银行卡业务','银行卡挂失后还能恢复吗？','挂失后还能解挂吗；临时挂失怎么取消；卡找到了还能用吗','挂失；解挂；恢复','临时挂失在规定期限内通常可以解除，正式挂失或已补卡后一般不能恢复原卡使用。具体以银行页面或网点审核结果为准。',1,80,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(8,'银行卡业务','如何补办银行卡？','补卡怎么办；银行卡坏了怎么换；换新卡需要什么','补卡；换卡；银行卡','补办银行卡通常需要本人携带有效身份证件到网点办理，部分银行支持线上申请后邮寄或到店领取。补卡后原卡可能失效，请及时更新绑定关系。',1,85,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(9,'银行卡业务','银行卡被吞卡怎么办？','ATM吞卡了；取款机把卡吞了；卡被机器吞了怎么取回','吞卡；ATM；取款机','银行卡被自助设备吞卡后，请保留设备位置和时间信息，尽快联系设备所属银行网点或客服电话处理。领取时通常需要本人身份证件。',1,75,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(10,'银行卡业务','如何设置银行卡交易提醒？','消费提醒怎么开；动账通知在哪里设置；短信提醒怎么开通','交易提醒；动账通知；短信提醒','你可以在手机银行的消息通知、账户安全或银行卡管理页面开启交易提醒。短信提醒可能涉及服务费用，具体以页面展示为准。',1,75,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(11,'转账汇款','转账多久到账？','转账什么时候到；跨行转账要多久；我转的钱多久能到账','转账；到账；跨行','转账到账时间与转账方式、金额、收款行和处理时间有关。行内转账通常较快，跨行转账可能实时、数小时或下一个工作日到账，具体以提交页面提示为准。',1,120,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(12,'转账汇款','跨行转账手续费怎么收？','跨行转账要手续费吗；转账手续费多少；给别的银行转钱收费吗','跨行；手续费；转账收费','跨行转账手续费与渠道、金额和账户类型有关。手机银行渠道通常会在提交前展示费用，实际收费请以转账确认页为准。',1,110,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(13,'转账汇款','转账失败怎么办？','钱没转出去怎么办；转账提示失败；转账被退回怎么处理','转账失败；退回；失败','转账失败时请先查看失败原因，例如账户状态、限额、收款信息或系统处理异常。若资金已扣除但未到账，请保留流水号并联系人工客服核查。',1,105,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(14,'转账汇款','每日转账限额是多少？','一天最多能转多少钱；转账额度是多少；每日限额怎么查','转账限额；每日限额；额度','每日转账限额与认证方式、账户类型和风险等级有关。你可以在手机银行的转账限额或安全设置页面查询当前限额。',1,100,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(15,'转账汇款','转账时收款人信息填错怎么办？','收款账号填错了；转错账户怎么办；名字写错会到账吗','收款人；填错；转错','如果转账信息填写错误，请立即查看交易状态。未提交可直接取消；已提交需根据处理状态联系银行客服，跨行交易还可能需要收款行协助处理。',1,95,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(16,'密码与安全','忘记登录密码怎么办？','忘记密码怎么办；密码忘了；网银登录不上；怎么重置登录密码','登录密码；忘记密码；重置密码','忘记登录密码时，可在登录页选择“忘记密码”并按提示完成身份验证后重置。若验证失败，请携带身份证件和银行卡到网点处理。',1,120,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(17,'密码与安全','忘记支付密码怎么办？','支付密码忘了；转账密码不记得；怎么重置支付密码','支付密码；交易密码；重置','忘记支付密码时，请通过手机银行安全中心或银行卡管理页面重置。系统会要求身份核验，无法通过时需到网点办理。',1,105,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(18,'密码与安全','验证码收不到怎么办？','手机验证码没收到；短信验证码不来；验证码一直收不到','验证码；短信；收不到','请先确认手机号是否为银行预留号码、手机信号是否正常、短信是否被拦截。仍无法收到时，可稍后重试或联系人工客服核查短信服务状态。',1,110,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(19,'密码与安全','如何防范电信诈骗？','怎么避免被骗；有人让我转账怎么办；如何识别诈骗','诈骗；防骗；风险','不要向陌生人透露密码、验证码、身份证号和银行卡信息。遇到要求转账、屏幕共享、下载陌生软件等情况，应先停止操作并联系官方客服核实。',1,95,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(20,'密码与安全','发现账户异常交易怎么办？','账户被盗刷；有一笔不是我花的；发现异常扣款','异常交易；盗刷；扣款','发现异常交易时请立即冻结或挂失相关银行卡，保存交易截图和短信记录，并尽快联系人工客服或到网点核查处理。',1,105,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(21,'网银/手机银行','如何开通手机银行？','手机银行怎么开；怎么注册手机银行；网银怎么开通','手机银行；开通；注册','你可以通过银行官方 App 自助注册手机银行，或携带身份证件和银行卡到网点开通。首次开通通常需要完成手机号和身份验证。',1,90,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(22,'网银/手机银行','手机银行登录不上怎么办？','App登录失败；手机银行进不去；网银无法登录','登录失败；手机银行；网银','请先检查网络、App版本、用户名密码和短信验证状态。若多次输入错误导致锁定，请按页面提示解锁或重置密码。',1,90,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(23,'网银/手机银行','如何绑定或解绑银行卡？','怎么绑卡；解绑银行卡在哪里；换一张卡绑定','绑卡；解绑银行卡；银行卡管理','你可以在手机银行的银行卡管理或账户管理页面绑定、解绑银行卡。解绑银行卡前请确认无未完成交易、代扣或贷款还款关系。',1,80,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(24,'网银/手机银行','如何查看电子回单？','转账回单在哪里；电子凭证怎么查；我要下载转账凭证','电子回单；转账凭证；回单','电子回单通常可在转账记录、交易详情或电子回单页面查看和下载。若交易刚完成，回单生成可能存在短暂延迟。',1,85,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(25,'网银/手机银行','如何关闭免密支付？','小额免密怎么关；快捷支付怎么取消；免密扣款怎么关闭','免密支付；快捷支付；关闭','你可以在支付管理、安全中心或银行卡快捷支付管理中关闭免密支付。关闭后，相关支付可能需要重新验证密码或验证码。',1,85,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(26,'贷款咨询','个人贷款需要准备什么材料？','贷款要什么资料；申请贷款材料；个人贷款怎么申请','贷款；材料；申请','个人贷款通常需要身份证明、收入证明、征信授权、用途材料等。不同贷款产品要求不同，请以申请页面或客户经理要求为准。',1,85,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(27,'贷款咨询','如何查询贷款进度？','贷款审批到哪了；怎么查贷款状态；贷款申请进度','贷款进度；审批；贷款状态','你可以在手机银行贷款页面查看申请进度，也可以联系客户经理或人工客服查询。查询时可能需要核验身份信息。',1,80,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(28,'贷款咨询','贷款提前还款怎么办理？','提前还贷怎么操作；贷款想提前还；提前还款流程','提前还款；提前还贷；贷款','提前还款可通过手机银行贷款页面预约或到网点办理。部分贷款可能涉及预约期限、违约金或利息计算，请以合同和页面提示为准。',1,80,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(29,'贷款咨询','房贷利率如何查询？','我的房贷利率是多少；贷款利率哪里看；怎么查按揭利率','房贷；利率；按揭','房贷利率可在贷款合同、手机银行贷款详情或还款计划中查询。若利率调整，请以银行最新通知和合同约定为准。',1,70,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(30,'贷款咨询','贷款逾期会有什么影响？','贷款晚还会怎样；还款逾期怎么办；贷款逾期影响征信吗','逾期；征信；还款','贷款逾期可能产生罚息，并可能影响个人征信。若预计无法按时还款，请尽快联系银行说明情况并确认可行处理方案。',1,95,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(31,'收费与限额','ATM跨行取款手续费如何收取？','取款手续费多少；跨行取现金收费吗；ATM取款怎么收费','ATM；取款；手续费','ATM跨行取款手续费与发卡行、取款行和地区政策有关，实际费用请以交易页面、网点公告或银行最新收费标准为准。',1,70,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(32,'收费与限额','短信通知服务收费吗？','短信提醒要钱吗；动账短信收费吗；账户短信通知费用','短信通知；收费；动账短信','短信通知服务是否收费取决于账户类型和服务套餐。你可以在消息通知或服务设置页面查看并开通、取消该服务。',1,70,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(33,'收费与限额','如何调整转账限额？','转账额度怎么提高；怎么调高限额；限额太低怎么办','调整限额；提高限额；转账额度','调整转账限额通常需要完成安全认证。你可以在手机银行安全设置中申请调整；超过线上可调范围时，请到网点办理。',1,95,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(34,'收费与限额','账户管理费如何收取？','账户管理费是什么；小额账户收费吗；为什么扣账户管理费','账户管理费；小额账户；收费','账户管理费与账户类型、余额、减免政策有关。若对扣费有疑问，可查看收费明细或联系人工客服核实。',1,65,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(35,'收费与限额','为什么会扣年费？','银行卡年费怎么扣；年费能免吗；被扣银行卡年费','年费；银行卡年费；扣费','银行卡年费取决于卡种和减免条件。部分账户满足条件可申请减免，具体规则请以银行收费标准和页面提示为准。',1,65,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(36,'人工客服/网点','如何联系人工客服？','我要找人工；怎么转人工客服；客服电话是多少','人工客服；转人工；客服','你可以在客服入口输入“人工客服”或拨打银行官方客服电话转人工。涉及账户信息时，客服会先进行身份核验。',1,100,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(37,'人工客服/网点','如何查询附近网点？','附近银行网点在哪；最近的支行；怎么找网点','网点；附近；支行','你可以在手机银行网点查询页面或地图服务中查询附近网点。建议办理前确认网点营业状态和可办理业务。',1,85,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(38,'人工客服/网点','网点营业时间如何查询？','银行几点上班；支行周末开门吗；营业时间在哪里看','营业时间；网点；上班','网点营业时间可能因地区、节假日和网点类型不同而变化。请在网点查询页面查看实时营业信息，或提前拨打网点电话确认。',1,85,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(39,'人工客服/网点','如何预约网点办理业务？','去网点怎么预约；预约取号；网点排队怎么预约','预约；网点；取号','你可以在手机银行网点预约或排队取号页面选择网点、业务类型和办理时间。预约成功后请按时携带相关材料前往。',1,75,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43'),(40,'人工客服/网点','如何提交服务投诉？','我要投诉；客服服务不好怎么办；怎么反馈问题','投诉；反馈；服务','如需投诉或反馈服务问题，可以通过官方客服、手机银行意见反馈或网点渠道提交。请尽量提供时间、业务类型和相关凭证，便于核查处理。',1,90,'毕业设计FAQ初始化数据','2026-04-30 10:36:43','2026-04-30 10:36:43');
/*!40000 ALTER TABLE `faq_knowledge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `faq_query_log`
--

DROP TABLE IF EXISTS `faq_query_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `faq_query_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '提问用户ID',
  `user_query` text NOT NULL COMMENT '用户原始问题',
  `matched_faq_id` bigint DEFAULT NULL COMMENT '命中的FAQ ID',
  `match_score` decimal(5,2) DEFAULT NULL COMMENT '匹配得分，百分制',
  `result_type` varchar(30) NOT NULL COMMENT 'hit, recommend, no_answer',
  `user_feedback` tinyint DEFAULT NULL COMMENT '1满意，0不满意，可为空',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_faq_log_user_time` (`user_id`,`create_time`),
  KEY `idx_faq_log_result_type` (`result_type`),
  KEY `idx_faq_log_matched_faq` (`matched_faq_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='FAQ查询日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `faq_query_log`
--

LOCK TABLES `faq_query_log` WRITE;
/*!40000 ALTER TABLE `faq_query_log` DISABLE KEYS */;
INSERT INTO `faq_query_log` VALUES (1,1,'我卡丢了怎么办？',6,54.67,'recommend',NULL,'2026-04-30 11:48:00'),(2,1,'银行卡丢失后如何处理？',6,94.00,'hit',NULL,'2026-04-30 11:48:25'),(3,1,'我想转账给另一个银行，一般花多长时间',11,46.00,'recommend',NULL,'2026-04-30 11:48:52'),(4,1,'跨行转账手续费怎么收？',12,94.00,'hit',NULL,'2026-04-30 11:49:09'),(5,1,'卡不见了要不要先挂失',6,46.00,'recommend',NULL,'2026-04-30 12:05:31'),(6,1,'银行卡丢失后如何处理？',6,94.00,'hit',NULL,'2026-04-30 12:05:46'),(7,1,'晚上转账会不会第二天才到',11,46.00,'recommend',NULL,'2026-04-30 12:05:56'),(8,1,'跨行转账手续费怎么收',12,94.00,'hit',NULL,'2026-04-30 12:06:13'),(9,1,'银行卡疑似丢失怎么冻结',6,60.24,'recommend',NULL,'2026-04-30 12:15:00'),(10,1,'晚上转账会不会第二天才到',11,46.00,'recommend',NULL,'2026-04-30 12:15:13'),(11,1,'卡可能丢了，我先干啥，挂失还是冻结',6,46.00,'recommend',NULL,'2026-04-30 14:32:07'),(12,1,'今晚跨行转账，最晚啥时候到',11,46.00,'recommend',NULL,'2026-04-30 14:32:29'),(13,1,'转账多久到账？',11,94.00,'hit',NULL,'2026-04-30 14:33:00'),(14,1,'我挺不爽的，想投诉一下',NULL,NULL,'no_answer',NULL,'2026-04-30 14:35:01'),(15,1,'银行卡找不到了我该先做哪一步',6,46.00,'recommend',NULL,'2026-04-30 14:36:05'),(16,1,'银行卡丢失后如何处理？',6,94.00,'hit',NULL,'2026-04-30 14:36:13');
/*!40000 ALTER TABLE `faq_query_log` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=6351 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction_record`
--

LOCK TABLES `transaction_record` WRITE;
/*!40000 ALTER TABLE `transaction_record` DISABLE KEYS */;
INSERT INTO `transaction_record` VALUES (1,1,'6222000098765432','收入',5000.00,'工资','2026-01-11 18:47:17'),(2,1,'6222000076543210','支出',-200.00,'购物','2026-01-11 18:47:17'),(7,1,'6222000076543210','支出',-2000.00,'有钱！大把挥霍！','2026-01-11 19:31:54'),(8,4,'6222000012345678','收入',2000.00,'有钱！大把挥霍！','2026-01-11 19:31:54'),(9,1,'6222000076543210','支出',-111.00,'22222','2026-01-17 09:32:14'),(10,4,'6222000012345678','收入',111.00,'22222','2026-01-17 09:32:14'),(11,1,'6222000076543210','支出',-200.00,'111','2026-01-17 10:09:19'),(12,4,'6222000012345678','收入',200.00,'111','2026-01-17 10:09:19'),(13,1,'6222000076543210','支出',-123.00,'转账逻辑测试','2026-01-17 10:21:08'),(14,4,'6222000012345678','收入',123.00,'转账逻辑测试','2026-01-17 10:21:08'),(15,1,'6222000076543210','支出',-123.00,'转账逻辑测试','2026-01-17 10:21:41'),(16,4,'6222000012345678','收入',123.00,'转账逻辑测试','2026-01-17 10:21:41'),(17,1,'6222000076543210','支出',-123.00,'转账逻辑测试222','2026-01-17 10:21:49'),(18,4,'6222000012345678','收入',123.00,'转账逻辑测试222','2026-01-17 10:21:49'),(19,1,'6222000076543210','支出',-123.00,'逻辑测试第二次','2026-01-17 10:25:32'),(20,4,'6222000012345678','收入',123.00,'逻辑测试第二次','2026-01-17 10:25:32'),(21,1,'6222000076543210','支出',-60.00,'测试逻辑第3次','2026-01-17 10:27:54'),(22,4,'6222000012345678','收入',60.00,'测试逻辑第3次','2026-01-17 10:27:54'),(23,1,'6222000076543210','支出',-73.00,'','2026-03-08 22:43:36'),(24,4,'6222000012345678','收入',73.00,'','2026-03-08 22:43:36'),(25,1,'6222000076543210','支出',-300.00,'','2026-03-08 23:09:09'),(26,4,'6222000012345678','收入',300.00,'','2026-03-08 23:09:09'),(27,1,'6222000076543210','支出',-250.00,'','2026-03-09 00:02:27'),(28,4,'6222000012345678','收入',250.00,'','2026-03-09 00:02:27'),(29,1,'6222000076543210','支出',-300.00,'测试5566','2026-03-17 12:58:06'),(30,4,'6222000012345678','收入',300.00,'测试5566','2026-03-17 12:58:06'),(31,1,'6222000076543210','支出',-487.00,'测试3333','2026-03-17 14:19:40'),(32,4,'6222000012345678','收入',487.00,'测试3333','2026-03-17 14:19:40'),(33,1,'6222000043210987','支出',-10000.00,'风险测试','2026-03-22 16:14:21'),(34,3,'6222000012345678','收入',10000.00,'风险测试','2026-03-22 16:14:21'),(35,3,'6222000012345678','支出',-10000.00,'大额支出','2026-04-07 14:37:33'),(36,1,'6222000043210987','收入',10000.00,'大额支出','2026-04-07 14:37:33'),(37,3,'6222000012345678','支出',-100.00,'小额','2026-04-07 14:37:42'),(38,1,'6222000043210987','收入',100.00,'小额','2026-04-07 14:37:42'),(39,3,'6222000012345678','支出',-1000.00,'小额','2026-04-07 14:37:52'),(40,1,'6222000043210987','收入',1000.00,'小额','2026-04-07 14:37:52'),(41,1,'6222000098765432','支出',-300.00,'弟弟','2026-04-23 10:55:17'),(42,2,'6222000012345678','收入',300.00,'弟弟','2026-04-23 10:55:17'),(43,1,'6222000098765432','支出',-300.00,'弟弟的午餐费','2026-04-21 10:55:38'),(44,2,'6222000012345678','收入',300.00,'弟弟的午餐费','2026-04-21 10:55:38'),(45,1,'6222000098765432','支出',-100.00,'晚餐费','2026-04-17 11:04:47'),(46,2,'6222000012345678','收入',100.00,'晚餐费','2026-04-17 11:04:47'),(47,1,'6222000043210987','支出',-3000.00,'团建费','2026-03-23 11:05:44'),(48,3,'6222000012345678','收入',3000.00,'团建费','2026-03-23 11:05:44'),(49,1,'6222000076543210','支出',-3000.00,'','2026-04-23 11:06:08'),(50,4,'6222000012345678','收入',3000.00,'','2026-04-23 11:06:08'),(51,1,'6222000098765432','支出',-100.00,'晚餐费','2026-04-23 11:22:44'),(52,2,'6222000012345678','收入',100.00,'晚餐费','2026-04-23 11:22:44'),(53,1,'6222000098765432','支出',-300.00,'午餐费','2026-04-23 11:44:56'),(54,2,'6222000012345678','收入',300.00,'午餐费','2026-04-23 11:44:56'),(55,1,'6222000098765432','支出',-100.00,'晚餐费','2026-04-23 11:45:23'),(56,2,'6222000012345678','收入',100.00,'晚餐费','2026-04-23 11:45:23'),(57,1,'6222000098765432','支出',-100.00,'晚餐费','2026-04-23 11:46:52'),(58,2,'6222000012345678','收入',100.00,'晚餐费','2026-04-23 11:46:52'),(59,1,'6222000043210987','支出',-3000.00,'团建费','2026-04-23 11:48:10'),(60,3,'6222000012345678','收入',3000.00,'团建费','2026-04-23 11:48:10'),(61,1,'6222000098765432','支出',-100.00,'晚餐费','2026-04-23 12:03:03'),(62,2,'6222000012345678','收入',100.00,'晚餐费','2026-04-23 12:03:03'),(63,1,'6222000098765432','支出',-300.00,'午餐费','2026-04-28 16:14:44'),(64,2,'6222000012345678','收入',300.00,'午餐费','2026-04-28 16:14:44'),(65,1,'6222000098765432','支出',-100.00,'晚餐费','2026-04-28 16:15:04'),(66,2,'6222000012345678','收入',100.00,'晚餐费','2026-04-28 16:15:04'),(67,1,'6222000043210987','支出',-3000.00,'团建费用','2026-04-28 16:23:01'),(68,3,'6222000012345678','收入',3000.00,'团建费用','2026-04-28 16:23:01'),(6301,1,'6222000098765432','收入',8800.00,'工资收入','2026-05-01 09:05:00'),(6302,2,'6222000012345678','支出',-8800.00,'工资收入','2026-05-01 09:05:00'),(6303,1,'6222000076543210','支出',-3200.00,'房租转账','2026-05-01 10:20:00'),(6304,4,'6222000012345678','收入',3200.00,'房租转账','2026-05-01 10:20:00'),(6305,1,'6222000043210987','支出',-86.50,'早餐咖啡','2026-05-01 12:18:00'),(6306,3,'6222000012345678','收入',86.50,'早餐咖啡','2026-05-01 12:18:00'),(6307,1,'6222000098765432','支出',-300.00,'弟弟午餐费','2026-05-01 18:40:00'),(6308,2,'6222000012345678','收入',300.00,'弟弟午餐费','2026-05-01 18:40:00'),(6309,1,'6222000076543210','支出',-268.80,'超市采购','2026-05-02 10:12:00'),(6310,4,'6222000012345678','收入',268.80,'超市采购','2026-05-02 10:12:00'),(6311,1,'6222000043210987','支出',-42.00,'地铁充值','2026-05-02 13:35:00'),(6312,3,'6222000012345678','收入',42.00,'地铁充值','2026-05-02 13:35:00'),(6313,1,'6222000043210987','收入',1200.00,'项目报销','2026-05-02 16:25:00'),(6314,3,'6222000012345678','支出',-1200.00,'项目报销','2026-05-02 16:25:00'),(6315,1,'6222000098765432','支出',-199.00,'云服务订阅','2026-05-02 21:10:00'),(6316,2,'6222000012345678','收入',199.00,'云服务订阅','2026-05-02 21:10:00'),(6317,1,'6222000076543210','支出',-568.00,'家电维修','2026-05-03 09:50:00'),(6318,4,'6222000012345678','收入',568.00,'家电维修','2026-05-03 09:50:00'),(6319,1,'6222000098765432','支出',-128.00,'周末聚餐','2026-05-03 12:30:00'),(6320,2,'6222000012345678','收入',128.00,'周末聚餐','2026-05-03 12:30:00'),(6321,1,'6222000043210987','支出',-39.90,'技术书籍','2026-05-03 15:45:00'),(6322,3,'6222000012345678','收入',39.90,'技术书籍','2026-05-03 15:45:00'),(6323,1,'6222000076543210','收入',88.00,'购物退款','2026-05-03 20:05:00'),(6324,4,'6222000012345678','支出',-88.00,'购物退款','2026-05-03 20:05:00'),(6325,1,'6222000043210987','支出',-1200.00,'信用卡还款','2026-05-04 09:30:00'),(6326,3,'6222000012345678','收入',1200.00,'信用卡还款','2026-05-04 09:30:00'),(6327,1,'6222000098765432','支出',-18.00,'地铁出行','2026-05-04 11:10:00'),(6328,2,'6222000012345678','收入',18.00,'地铁出行','2026-05-04 11:10:00'),(6329,1,'6222000076543210','支出',-256.00,'医药费','2026-05-04 15:42:00'),(6330,4,'6222000012345678','收入',256.00,'医药费','2026-05-04 15:42:00'),(6331,1,'6222000098765432','收入',520.00,'差旅补贴','2026-05-04 19:08:00'),(6332,2,'6222000012345678','支出',-520.00,'差旅补贴','2026-05-04 19:08:00'),(6333,1,'6222000098765432','支出',-2000.00,'弟弟生活费','2026-05-05 08:55:00'),(6334,2,'6222000012345678','收入',2000.00,'弟弟生活费','2026-05-05 08:55:00'),(6335,1,'6222000043210987','支出',-688.00,'手机配件','2026-05-05 13:20:00'),(6336,3,'6222000012345678','收入',688.00,'手机配件','2026-05-05 13:20:00'),(6337,1,'6222000076543210','支出',-75.60,'晚餐费','2026-05-05 18:35:00'),(6338,4,'6222000012345678','收入',75.60,'晚餐费','2026-05-05 18:35:00'),(6339,1,'6222000043210987','支出',-35.00,'打车','2026-05-05 22:15:00'),(6340,3,'6222000012345678','收入',35.00,'打车','2026-05-05 22:15:00'),(6341,1,'6222000098765432','收入',3000.00,'绩效奖金','2026-05-06 09:18:00'),(6342,2,'6222000012345678','支出',-3000.00,'绩效奖金','2026-05-06 09:18:00'),(6343,1,'6222000076543210','支出',-138.00,'水电燃气','2026-05-06 10:45:00'),(6344,4,'6222000012345678','收入',138.00,'水电燃气','2026-05-06 10:45:00'),(6345,1,'6222000043210987','支出',-420.00,'健身卡','2026-05-06 14:05:00'),(6346,3,'6222000012345678','收入',420.00,'健身卡','2026-05-06 14:05:00'),(6347,1,'6222000098765432','支出',-300.00,'弟弟晚餐费','2026-05-06 18:22:00'),(6348,2,'6222000012345678','收入',300.00,'弟弟晚餐费','2026-05-06 18:22:00'),(6349,1,'6222000076543210','支出',-5200.00,'笔记本电脑','2026-05-06 20:40:00'),(6350,4,'6222000012345678','收入',5200.00,'笔记本电脑','2026-05-06 20:40:00');
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

-- Dump completed on 2026-05-12 10:00:53
