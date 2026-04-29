package com.idea.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idea.assistant.PendingTransfer;
import com.idea.common.ErrorCode;
import com.idea.dto.*;
import com.idea.entity.Account;
import com.idea.entity.FaqKnowledge;
import com.idea.entity.PayeeContact;
import com.idea.entity.Result;
import com.idea.entity.TransactionRecord;
import com.idea.exception.BusinessException;
import com.idea.llm.QwenClient;
import com.idea.mapper.PayeeContactMapper;
import com.idea.mapper.TransactionMapper;
import com.idea.service.impl.AccountServiceImpl;
import com.idea.service.impl.TransactionServiceImpl;
import com.idea.vo.AssistantChatResponse;
import com.idea.vo.StatisticsPointVO;
import com.idea.vo.StatisticsResultVO;
import com.idea.vo.TransferValidateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.idea.utils.Time;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantService {

    private final QwenClient qwenClient;
    private final AccountServiceImpl accountService;
    private final TransactionServiceImpl transactionService;
    private final StatisticsService statisticsService;
    private final PayeeService payeeService;
    private final FaqKnowledgeService faqKnowledgeService;
    private final PayeeContactMapper payeeContactMapper;
    private final TransactionMapper transactionMapper;


    private final Map<Long, PendingTransfer> pendingMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, PendingTransactionQuery> pendingTxnQueryMap = new ConcurrentHashMap<>();
    private static final Duration PENDING_TTL = Duration.ofMinutes(10);
    private static final String HUMAN_SERVICE_PHONE = "010-1234-5678";

    public Result chat(Long userId, AssistantChatRequest req) {
        String msg = req.getMessage() == null ? "" : req.getMessage().trim();
        if (msg.isEmpty()) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "message 不能为空");
        }
        log.info("assistant chat start, userId={}, message={}", userId, msg);

        // 1. 优先处理转账确认/取消
        Result pendingResult = handlePendingTransfer(userId, msg);
        if (pendingResult != null) {
            return pendingResult;
        }

        // 2. 优先处理“查17号收入记录 -> 用户补月份”这种多轮查询
        Result pendingTxnResult = handlePendingTransactionQuery(userId, msg);
        if (pendingTxnResult != null) {
            return pendingTxnResult;
        }

        // 3. 情绪异常或投诉类表达优先进入安抚与人工客服引导
        Result emotionResult = handleEmotionSupport(msg);
        if (emotionResult != null) {
            return emotionResult;
        }

        // 4. 解析意图
        JsonNode parsed = parseIntentByQwen(msg);
        parsed = Time.normalizeTimeByText(msg, parsed);
        String intent = parsed.path("intent").asText("");
        log.info("assistant intent parsed, userId={}, intent={}", userId, intent);

        // 5. 分发处理
        switch (intent) {
            case "transfer":
                return handleTransfer(userId, parsed, msg);
            case "transactions":
                return handleTransactions(userId, parsed);
            case "balance":
                return handleBalance(userId);
            case "statistics":
                return handleStatistics(userId, parsed);
            default:
                return handleFaqOrFallback(userId, msg);
        }

    }

    public SseEmitter chatStream(Long userId, AssistantChatRequest req) {
        SseEmitter emitter = new SseEmitter(120000L);
        CompletableFuture.runAsync(() -> {
            try {
                sendEvent(emitter, "trace", Map.of("phase", "received", "message", "请求已接收，正在准备解析意图"));
                sendEvent(emitter, "trace", Map.of("phase", "intent", "message", "正在识别你的业务意图"));
                Result result = chat(userId, req);
                if (result.getCode() != null && result.getCode() == 1) {
                    sendEvent(emitter, "message", result.getData());
                } else {
                    sendEvent(emitter, "error", Map.of("message", safe(result.getMsg())));
                }
                sendEvent(emitter, "done", Map.of("ok", true));
                emitter.complete();
            } catch (Exception ex) {
                log.error("assistant chat stream failed, userId={}", userId, ex);
                try {
                    sendEvent(emitter, "error", Map.of("message", "流式请求失败，请稍后重试"));
                    sendEvent(emitter, "done", Map.of("ok", false));
                } catch (IOException ioException) {
                    log.warn("assistant stream send error event failed, userId={}", userId, ioException);
                }
                emitter.complete();
            }
        });
        return emitter;
    }

    public Result getBrief(Long userId, String period) {
        Account account = accountService.getAccountInfo(userId);
        if (account == null) {
            throw BusinessException.of(ErrorCode.ACCOUNT_NOT_FOUND, "账户不存在");
        }
        String normalizedPeriod = ("month".equalsIgnoreCase(period) || "week".equalsIgnoreCase(period))
                ? period.toLowerCase()
                : "week";

        StatisticsQueryDTO incomeQuery = buildBriefQuery(account.getId(), "收入", "sum", normalizedPeriod);
        StatisticsQueryDTO expenseQuery = buildBriefQuery(account.getId(), "支出", "sum", normalizedPeriod);
        StatisticsQueryDTO trendQuery = buildBriefQuery(account.getId(), "支出", "trend", normalizedPeriod);

        StatisticsResultVO incomeVo = statisticsService.statistics(incomeQuery);
        StatisticsResultVO expenseVo = statisticsService.statistics(expenseQuery);
        StatisticsResultVO trendVo = statisticsService.statistics(trendQuery);

        BigDecimal income = incomeVo.getValue() == null ? BigDecimal.ZERO : incomeVo.getValue();
        BigDecimal expense = expenseVo.getValue() == null ? BigDecimal.ZERO : expenseVo.getValue();
        BigDecimal net = income.subtract(expense);
        String periodLabel = "month".equals(normalizedPeriod) ? "本月" : "本周";

        List<String> highlights = new ArrayList<>();
        highlights.add(periodLabel + "总收入：¥" + income.toPlainString());
        highlights.add(periodLabel + "总支出：¥" + expense.toPlainString());
        highlights.add(periodLabel + "净流入：¥" + net.toPlainString());

        Map<String, Object> comparison = buildBriefComparison(account.getId(), normalizedPeriod, income, expense);
        List<String> suggestions = generateBriefSuggestions(normalizedPeriod, income, expense, net, trendVo.getTrendList());
        List<Map<String, String>> anomalies = detectExpenseAnomaly(account.getId(), expense, normalizedPeriod);
        String anomaly = anomalies.isEmpty() ? "" : anomalies.get(0).get("message");
        List<String> quickActions = List.of(
                "看" + periodLabel + "支出明细",
                "统计" + periodLabel + "总支出",
                "看" + periodLabel + "收入趋势"
        );

        Map<String, Object> brief = new HashMap<>();
        brief.put("period", normalizedPeriod);
        brief.put("headline", "AI简报：" + periodLabel + "净流入 ¥" + net.toPlainString());
        brief.put("highlights", highlights);
        brief.put("anomaly", anomaly);
        brief.put("anomalies", anomalies);
        brief.put("suggestions", suggestions);
        brief.put("comparison", comparison);
        brief.put("quickActions", quickActions);
        brief.put("chartType", "bar");
        brief.put("chartData", trendVo.getTrendList() == null ? List.of() : trendVo.getTrendList());
        return Result.success(brief);
    }

    //AI数据报表统计
    private Result handleStatistics(Long userId, JsonNode parsed) {
        String txnType = parsed.path("txnType").asText("");
        String metric = parsed.path("metric").asText("");
        String groupBy = parsed.path("groupBy").asText("");
        String yearStr = parsed.path("year").asText("");
        String monthStr = parsed.path("month").asText("");
        String dayStr = parsed.path("day").asText("");
        String timeRange = parsed.path("timeRange").asText("");
        String rangeValueStr = parsed.path("rangeValue").asText("");
        boolean comparePrevious = parsed.path("comparePrevious").asBoolean(false);

        if (metric.isBlank()) {
            AssistantChatResponse response = new AssistantChatResponse(
                    "你可以这样说：统计这个月总支出、最近7天我花了多少钱、本月有多少笔收入、统计今年每个月的支出。"
            );
            attachTrace(response, "statistics", "statistics_service", "need_more_info", "high", timeRange,
                    List.of("识别为统计意图", "缺少统计指标 metric"));
            return Result.success(response);
        }

        Account account = accountService.getAccountInfo(userId);
        if (account == null) {
            throw BusinessException.of(ErrorCode.ACCOUNT_NOT_FOUND, "账户不存在");
        }

        StatisticsQueryDTO q = new StatisticsQueryDTO();
        q.setAccountId(account.getId());
        q.setTxnType(txnType.isBlank() ? null : txnType);
        q.setMetric(metric);
        q.setGroupBy(groupBy.isBlank() ? "none" : groupBy);
        q.setComparePrevious(comparePrevious);

        Time.fillTimeRange(q, timeRange, yearStr, monthStr, dayStr, rangeValueStr);

        StatisticsResultVO vo = statisticsService.statistics(q);
        String reply = summarizeStatisticsByQwen(q, vo);
        
        // 构造返回给前端的响应
        AssistantChatResponse response = new AssistantChatResponse(reply);
        
        // 生成导出流水的基础链接
        StringBuilder urlBuilder = new StringBuilder("/transactions/export?accountId=").append(q.getAccountId());
        if (q.getTxnType() != null) {
            urlBuilder.append("&type=").append(q.getTxnType());
        }
        if (q.getStartTime() != null) {
            urlBuilder.append("&startTime=").append(q.getStartTime().substring(0, 10));
        }
        if (q.getEndTime() != null) {
            urlBuilder.append("&endTime=").append(q.getEndTime().substring(0, 10));
        }
        response.setExportUrl(urlBuilder.toString());
        
        // 生成图表数据
        if ("trend".equals(metric) && vo.getTrendList() != null && !vo.getTrendList().isEmpty()) {
            response.setChartType("bar"); // 默认为柱状图，前端可按需切换为 line
            response.setChartData(vo.getTrendList());
        } else if ("sum".equals(metric)) {
            // 如果返回了概览多图数据
            if (vo.getIncomeExpenseBar() != null) {
                response.setChartType("overview");
                response.setChartData(java.util.Map.of(
                        "bar", vo.getIncomeExpenseBar(),
                        "incomePie", vo.getIncomePie() != null ? vo.getIncomePie() : java.util.List.of(),
                        "expensePie", vo.getExpensePie() != null ? vo.getExpensePie() : java.util.List.of()
                ));
            } else {
                response.setChartType("number");
                response.setChartData(vo.getValue() != null ? vo.getValue() : vo.getCount());
            }
        } else if ("count".equals(metric)) {
            response.setChartType("number");
            response.setChartData(vo.getCount());
        } else if ("max".equals(metric)) {
            response.setChartType("number");
            response.setChartData(vo.getValue());
        }

        response.setExplain(Map.of(
                "metric", safe(q.getMetric()),
                "txnType", safe(q.getTxnType()),
                "groupBy", safe(q.getGroupBy()),
                "startTime", safe(q.getStartTime()),
                "endTime", safe(q.getEndTime()),
                "comparePrevious", q.getComparePrevious() != null && q.getComparePrevious()
        ));
        attachTrace(response, "statistics", "statistics_service", "completed", "high", timeRange,
                List.of("识别为统计意图", "查询统计服务", "生成分析总结"));

        return Result.success(response);
    }


    //分析型总结功能
    private String summarizeStatisticsByQwen(StatisticsQueryDTO q, StatisticsResultVO vo) {
        try {
            StatisticsInsight insight = buildStatisticsInsight(q, vo);
            String facts = buildStatisticsFacts(q, vo, insight);
            String ruleBasedFallback = buildRuleBasedStatisticsSummary(q, vo, insight);

            JsonNode raw = qwenClient.chat(List.of(
                    Map.of(
                            "role", "system",
                            "content", """
你是银行智能分析助手。
你的任务是：基于系统已给出的真实统计与流水分析结果，输出详细、可落地的账单分析。

要求：
1. 只能基于给定事实总结，禁止编造数据。
2. 语气自然、专业，像银行客户经理做月度复盘。
3. 控制在 4~8 句，优先覆盖：总体收支、主要收入日、主要支出日、主要对方账号、异常信号。
4. 至少引用 2 处具体数字（金额/日期/占比均可）。
5. 若数据为空或为 0，要明确说明未查询到明显记录或金额很少。
6. 禁止输出空话模板，例如“未区分收入与支出”“暂无进一步分项或趋势对比数据”。
7. 仅进行总结分析，不要要求用户继续补数据，不要输出 JSON。
8. 当前系统不支持支出品类、消费场景、商户类型分类分析，禁止输出此类结论。
9. 饼图仅表示按对方账号聚合，不得称为“消费分类”。
"""
                    ),
                    Map.of(
                            "role", "user",
                            "content", facts
                    )
            ));

            String content = raw.path("choices").get(0).path("message").path("content").asText("").trim();
            if (content == null || content.isBlank()) {
                return ruleBasedFallback;
            }
            content = sanitizeModelReply(content, ruleBasedFallback);
            if (isGenericStatisticsReply(content)) {
                return ruleBasedFallback;
            }
            return content;
        } catch (Exception e) {
            log.warn("assistant summarize statistics fallback, metric={}, accountId={}", q.getMetric(), q.getAccountId(), e);
            return buildRuleBasedStatisticsSummary(q, vo, buildStatisticsInsight(q, vo));
        }
    }

    //数据整理
    private String buildStatisticsFacts(StatisticsQueryDTO q, StatisticsResultVO vo, StatisticsInsight insight) {
        StringBuilder sb = new StringBuilder();

        sb.append("下面是系统已计算出的真实统计结果，请据此生成用户可直接阅读的分析总结：\n");

        sb.append("统计标题：").append(safe(vo.getTitle())).append("\n");
        sb.append("统计类型：").append(safe(q.getTxnType())).append("\n");
        sb.append("统计指标：").append(safe(q.getMetric())).append("\n");
        sb.append("分组方式：").append(safe(q.getGroupBy())).append("\n");
        sb.append("开始时间：").append(safe(q.getStartTime())).append("\n");
        sb.append("结束时间：").append(safe(q.getEndTime())).append("\n");

        if (vo.getValue() != null) {
            sb.append("统计值：").append(vo.getValue().toPlainString()).append("\n");
        }
        if (vo.getCount() != null) {
            sb.append("笔数：").append(vo.getCount()).append("\n");
        }

        if (vo.getTrendList() != null && !vo.getTrendList().isEmpty()) {
            sb.append("趋势明细：\n");
            for (StatisticsPointVO point : vo.getTrendList()) {
                sb.append("- ")
                        .append(safe(point.getLabel()))
                        .append(" : ")
                        .append(point.getValue() == null ? "0" : point.getValue().toPlainString())
                        .append("\n");
            }
        }

        appendInsightFacts(sb, insight);
        sb.append("系统规则总结：").append(safe(vo.getSummary())).append("\n");
        return sb.toString();
    }

    private StatisticsInsight buildStatisticsInsight(StatisticsQueryDTO q, StatisticsResultVO vo) {
        BigDecimal incomeTotal = extractBarAmount(vo.getIncomeExpenseBar(), "收入");
        BigDecimal expenseTotal = extractBarAmount(vo.getIncomeExpenseBar(), "支出");

        if ("收入".equals(q.getTxnType())) {
            incomeTotal = safeAmount(vo.getValue());
        } else if ("支出".equals(q.getTxnType())) {
            expenseTotal = safeAmount(vo.getValue());
        }

        StatisticsQueryDTO incomeSumQuery = buildStatisticsSubQuery(q, "收入", "sum", "none");
        StatisticsQueryDTO expenseSumQuery = buildStatisticsSubQuery(q, "支出", "sum", "none");
        if (incomeTotal == null) {
            incomeTotal = safeAmount(transactionMapper.sumAmount(incomeSumQuery));
        }
        if (expenseTotal == null) {
            expenseTotal = safeAmount(transactionMapper.sumAmount(expenseSumQuery));
        }

        List<StatisticsPointVO> incomeDayTrend = transactionMapper.statisticsTrend(buildStatisticsSubQuery(q, "收入", "trend", "day"));
        List<StatisticsPointVO> expenseDayTrend = transactionMapper.statisticsTrend(buildStatisticsSubQuery(q, "支出", "trend", "day"));
        StatisticsPointVO peakIncomeDay = maxValuePoint(incomeDayTrend);
        StatisticsPointVO peakExpenseDay = maxValuePoint(expenseDayTrend);

        List<StatisticsPointVO> topIncomeCounterparty = topNPoints(
                (vo.getIncomePie() != null && !vo.getIncomePie().isEmpty()) ? vo.getIncomePie() : transactionMapper.statisticsByCounterparty(incomeSumQuery),
                3
        );
        List<StatisticsPointVO> topExpenseCounterparty = topNPoints(
                (vo.getExpensePie() != null && !vo.getExpensePie().isEmpty()) ? vo.getExpensePie() : transactionMapper.statisticsByCounterparty(expenseSumQuery),
                3
        );

        StatisticsPointVO maxIncomeTxn = transactionMapper.maxTransaction(buildStatisticsSubQuery(q, "收入", "max", "none"));
        StatisticsPointVO maxExpenseTxn = transactionMapper.maxTransaction(buildStatisticsSubQuery(q, "支出", "max", "none"));

        Long incomeCount = transactionMapper.countByStatistics(buildStatisticsSubQuery(q, "收入", "count", "none"));
        Long expenseCount = transactionMapper.countByStatistics(buildStatisticsSubQuery(q, "支出", "count", "none"));

        List<String> anomalySignals = buildAnomalySignals(
                incomeTotal,
                expenseTotal,
                peakIncomeDay,
                peakExpenseDay,
                topIncomeCounterparty,
                topExpenseCounterparty,
                maxExpenseTxn,
                incomeCount,
                expenseCount
        );

        return new StatisticsInsight(
                safeAmount(incomeTotal),
                safeAmount(expenseTotal),
                peakIncomeDay,
                peakExpenseDay,
                topIncomeCounterparty,
                topExpenseCounterparty,
                maxIncomeTxn,
                maxExpenseTxn,
                incomeCount == null ? 0L : incomeCount,
                expenseCount == null ? 0L : expenseCount,
                anomalySignals
        );
    }

    private StatisticsQueryDTO buildStatisticsSubQuery(StatisticsQueryDTO source, String txnType, String metric, String groupBy) {
        StatisticsQueryDTO dto = new StatisticsQueryDTO();
        dto.setAccountId(source.getAccountId());
        dto.setAccountNo(source.getAccountNo());
        dto.setStartTime(source.getStartTime());
        dto.setEndTime(source.getEndTime());
        dto.setRangeValue(source.getRangeValue());
        dto.setComparePrevious(source.getComparePrevious());
        dto.setTxnType(txnType);
        dto.setMetric(metric);
        dto.setGroupBy(groupBy);
        return dto;
    }

    private BigDecimal extractBarAmount(List<StatisticsPointVO> bars, String label) {
        if (bars == null || bars.isEmpty()) {
            return null;
        }
        for (StatisticsPointVO point : bars) {
            if (point == null || point.getLabel() == null) {
                continue;
            }
            if (label.equals(point.getLabel())) {
                return point.getValue();
            }
        }
        return null;
    }

    private StatisticsPointVO maxValuePoint(List<StatisticsPointVO> points) {
        if (points == null || points.isEmpty()) {
            return null;
        }
        StatisticsPointVO max = null;
        for (StatisticsPointVO point : points) {
            if (point == null || point.getValue() == null) {
                continue;
            }
            if (max == null || point.getValue().compareTo(max.getValue()) > 0) {
                max = point;
            }
        }
        return max;
    }

    private List<StatisticsPointVO> topNPoints(List<StatisticsPointVO> points, int n) {
        if (points == null || points.isEmpty()) {
            return List.of();
        }
        int limit = Math.min(Math.max(n, 0), points.size());
        List<StatisticsPointVO> result = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            StatisticsPointVO point = points.get(i);
            if (point != null) {
                result.add(point);
            }
        }
        return result;
    }

    private void appendInsightFacts(StringBuilder sb, StatisticsInsight insight) {
        sb.append("补充流水分析数据：\n");
        sb.append("收入总额：").append(insight.totalIncome().toPlainString()).append("\n");
        sb.append("支出总额：").append(insight.totalExpense().toPlainString()).append("\n");
        sb.append("净流入：").append(insight.totalIncome().subtract(insight.totalExpense()).toPlainString()).append("\n");
        sb.append("收入笔数：").append(insight.incomeCount()).append("\n");
        sb.append("支出笔数：").append(insight.expenseCount()).append("\n");

        if (insight.peakIncomeDay() != null && insight.peakIncomeDay().getValue() != null) {
            sb.append("主要收入日：")
                    .append(safe(insight.peakIncomeDay().getLabel()))
                    .append("，金额 ")
                    .append(insight.peakIncomeDay().getValue().toPlainString())
                    .append("\n");
        }
        if (insight.peakExpenseDay() != null && insight.peakExpenseDay().getValue() != null) {
            sb.append("主要支出日：")
                    .append(safe(insight.peakExpenseDay().getLabel()))
                    .append("，金额 ")
                    .append(insight.peakExpenseDay().getValue().toPlainString())
                    .append("\n");
        }
        if (insight.maxIncomeTxn() != null && insight.maxIncomeTxn().getValue() != null) {
            sb.append("最大单笔收入：")
                    .append(insight.maxIncomeTxn().getValue().toPlainString())
                    .append("（")
                    .append(safe(insight.maxIncomeTxn().getLabel()))
                    .append("）\n");
        }
        if (insight.maxExpenseTxn() != null && insight.maxExpenseTxn().getValue() != null) {
            sb.append("最大单笔支出：")
                    .append(insight.maxExpenseTxn().getValue().toPlainString())
                    .append("（")
                    .append(safe(insight.maxExpenseTxn().getLabel()))
                    .append("）\n");
        }
        if (!insight.topIncomeCounterparty().isEmpty()) {
            sb.append("收入对方账号Top3：").append(formatCounterpartyTop(insight.topIncomeCounterparty())).append("\n");
        }
        if (!insight.topExpenseCounterparty().isEmpty()) {
            sb.append("支出对方账号Top3：").append(formatCounterpartyTop(insight.topExpenseCounterparty())).append("\n");
        }
        if (!insight.anomalySignals().isEmpty()) {
            sb.append("异常信号：\n");
            for (String signal : insight.anomalySignals()) {
                sb.append("- ").append(signal).append("\n");
            }
        }
    }

    private String formatCounterpartyTop(List<StatisticsPointVO> points) {
        List<String> parts = new ArrayList<>();
        for (StatisticsPointVO point : points) {
            if (point == null || point.getValue() == null) {
                continue;
            }
            parts.add(maskCounterparty(point.getLabel()) + "：" + point.getValue().toPlainString());
        }
        return parts.isEmpty() ? "无" : String.join("；", parts);
    }

    private String maskCounterparty(String accountNo) {
        String no = safe(accountNo);
        if (no.isBlank() || "未知".equals(no)) {
            return "未知";
        }
        if (no.length() < 8) {
            return no;
        }
        return no.substring(0, 4) + "****" + no.substring(no.length() - 4);
    }

    private List<String> buildAnomalySignals(BigDecimal incomeTotal,
                                             BigDecimal expenseTotal,
                                             StatisticsPointVO peakIncomeDay,
                                             StatisticsPointVO peakExpenseDay,
                                             List<StatisticsPointVO> topIncomeCounterparty,
                                             List<StatisticsPointVO> topExpenseCounterparty,
                                             StatisticsPointVO maxExpenseTxn,
                                             Long incomeCount,
                                             Long expenseCount) {
        List<String> signals = new ArrayList<>();

        BigDecimal safeExpenseTotal = safeAmount(expenseTotal);
        if (safeExpenseTotal.compareTo(BigDecimal.ZERO) > 0 && peakExpenseDay != null && peakExpenseDay.getValue() != null) {
            BigDecimal ratio = peakExpenseDay.getValue()
                    .divide(safeExpenseTotal, 4, RoundingMode.HALF_UP);
            if (ratio.compareTo(new BigDecimal("0.35")) >= 0) {
                signals.add("支出在 " + safe(peakExpenseDay.getLabel()) + " 明显集中，占总支出约 " + formatPercent(ratio) + "。");
            }
        }

        if (safeExpenseTotal.compareTo(BigDecimal.ZERO) > 0 && topExpenseCounterparty != null && !topExpenseCounterparty.isEmpty()) {
            StatisticsPointVO top1 = topExpenseCounterparty.get(0);
            if (top1 != null && top1.getValue() != null) {
                BigDecimal ratio = top1.getValue().divide(safeExpenseTotal, 4, RoundingMode.HALF_UP);
                if (ratio.compareTo(new BigDecimal("0.40")) >= 0) {
                    signals.add("对账号 " + maskCounterparty(top1.getLabel()) + " 的支出占比较高，约 " + formatPercent(ratio) + "。");
                }
            }
        }

        if (safeExpenseTotal.compareTo(BigDecimal.ZERO) > 0
                && maxExpenseTxn != null
                && maxExpenseTxn.getValue() != null
                && expenseCount != null
                && expenseCount > 0) {
            BigDecimal avgExpense = safeExpenseTotal.divide(BigDecimal.valueOf(expenseCount), 2, RoundingMode.HALF_UP);
            if (avgExpense.compareTo(BigDecimal.ZERO) > 0
                    && maxExpenseTxn.getValue().compareTo(avgExpense.multiply(BigDecimal.valueOf(3))) >= 0) {
                signals.add("存在明显大额单笔支出，单笔 " + maxExpenseTxn.getValue().toPlainString()
                        + "，约为平均单笔支出的 " + formatMultiple(maxExpenseTxn.getValue(), avgExpense) + " 倍。");
            }
        }

        BigDecimal safeIncomeTotal = safeAmount(incomeTotal);
        if (safeIncomeTotal.compareTo(BigDecimal.ZERO) > 0 && peakIncomeDay != null && peakIncomeDay.getValue() != null) {
            BigDecimal ratio = peakIncomeDay.getValue()
                    .divide(safeIncomeTotal, 4, RoundingMode.HALF_UP);
            if (ratio.compareTo(new BigDecimal("0.50")) >= 0) {
                signals.add("收入在 " + safe(peakIncomeDay.getLabel()) + " 较为集中，占总收入约 " + formatPercent(ratio) + "。");
            }
        }

        if (safeIncomeTotal.compareTo(BigDecimal.ZERO) > 0 && topIncomeCounterparty != null && !topIncomeCounterparty.isEmpty()) {
            StatisticsPointVO top1 = topIncomeCounterparty.get(0);
            if (top1 != null && top1.getValue() != null) {
                BigDecimal ratio = top1.getValue().divide(safeIncomeTotal, 4, RoundingMode.HALF_UP);
                if (ratio.compareTo(new BigDecimal("0.60")) >= 0) {
                    signals.add("收入来源对账号 " + maskCounterparty(top1.getLabel()) + " 依赖度较高，约 " + formatPercent(ratio) + "。");
                }
            }
        }

        if (incomeCount != null && incomeCount == 0 && expenseCount != null && expenseCount == 0) {
            signals.add("当前周期内没有有效流水。");
        }

        return signals;
    }

    private String formatPercent(BigDecimal ratio) {
        return ratio.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private String formatMultiple(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return "0.0";
        }
        return numerator.divide(denominator, 1, RoundingMode.HALF_UP).toPlainString();
    }

    private String buildRuleBasedStatisticsSummary(StatisticsQueryDTO q, StatisticsResultVO vo, StatisticsInsight insight) {
        StringBuilder sb = new StringBuilder();
        String periodLabel = buildStatisticsPeriodLabel(q);
        BigDecimal net = insight.totalIncome().subtract(insight.totalExpense());

        sb.append(periodLabel)
                .append("流水总览：收入 ")
                .append(insight.totalIncome().toPlainString())
                .append(" 元，支出 ")
                .append(insight.totalExpense().toPlainString())
                .append(" 元，净流入 ")
                .append(net.toPlainString())
                .append(" 元。");

        if (insight.peakIncomeDay() != null && insight.peakIncomeDay().getValue() != null) {
            sb.append("主要收入日为 ")
                    .append(safe(insight.peakIncomeDay().getLabel()))
                    .append("，当日收入 ")
                    .append(insight.peakIncomeDay().getValue().toPlainString())
                    .append(" 元。");
        }
        if (insight.peakExpenseDay() != null && insight.peakExpenseDay().getValue() != null) {
            sb.append("主要支出日为 ")
                    .append(safe(insight.peakExpenseDay().getLabel()))
                    .append("，当日支出 ")
                    .append(insight.peakExpenseDay().getValue().toPlainString())
                    .append(" 元。");
        }

        if (!insight.topIncomeCounterparty().isEmpty()) {
            sb.append("主要收入对方账号包括 ")
                    .append(formatCounterpartyTop(insight.topIncomeCounterparty()))
                    .append("。");
        }
        if (!insight.topExpenseCounterparty().isEmpty()) {
            sb.append("主要支出对方账号包括 ")
                    .append(formatCounterpartyTop(insight.topExpenseCounterparty()))
                    .append("。");
        }

        if (!insight.anomalySignals().isEmpty()) {
            sb.append("异常提示：").append(String.join("；", insight.anomalySignals())).append("。");
        }

        if (insight.incomeCount() == 0 && insight.expenseCount() == 0 && vo.getValue() != null) {
            sb.append("当前统计值为 ").append(vo.getValue().toPlainString()).append(" 元。");
        }
        return sb.toString();
    }

    private String buildStatisticsPeriodLabel(StatisticsQueryDTO q) {
        String startDate = extractDate(q.getStartTime());
        String endDate = extractDate(q.getEndTime());
        if (startDate.isBlank() || endDate.isBlank()) {
            return "当前周期";
        }
        if (startDate.length() >= 7
                && endDate.length() >= 7
                && startDate.substring(0, 7).equals(endDate.substring(0, 7))) {
            return startDate.substring(0, 7);
        }
        return startDate + "至" + endDate;
    }

    private boolean isGenericStatisticsReply(String content) {
        if (content == null || content.isBlank()) {
            return true;
        }
        return content.contains("未区分收入与支出")
                || content.contains("暂无进一步分项或趋势对比数据")
                || content.length() < 20;
    }



    /**
     * 处理待确认转账
     */
    private Result handlePendingTransfer(Long userId, String msg) {
        PendingTransfer pending = pendingMap.get(userId);
        if (pending == null) {
            return null;
        }
        if (isPendingExpired(pending.getCreatedAt())) {
            pendingMap.remove(userId);
            AssistantChatResponse response = new AssistantChatResponse("上一次转账确认已超时，请重新发起转账请求。");
            attachTrace(response, "transfer", "transfer_validation", "expired", "high", "",
                    List.of("检测到待确认转账", "会话超时自动失效"));
            return Result.success(response);
        }

        if (isConfirm(msg)) {
            if (pending.getPayeeId() != null) {
                PayeeTransferDTO dto = new PayeeTransferDTO();
                dto.setAmount(pending.getAmount());
                dto.setDescription(pending.getDescription());
                payeeService.transfer(userId, pending.getPayeeId(), dto);
            } else {
                TransferRequestDTO dto = new TransferRequestDTO();
                dto.setToAccountNo(pending.getToAccountNo());
                dto.setAmount(pending.getAmount());
                dto.setDescription(pending.getDescription());
                accountService.transfer(userId, dto);
            }
            pendingMap.remove(userId);
            AssistantChatResponse response = new AssistantChatResponse("已确认✅ 转账成功。");
            attachTrace(response, "transfer", "account_transfer", "completed", "high", "",
                    List.of("检测到待确认转账", "用户确认", "执行转账"));
            return Result.success(response);
        }

        if (isCancel(msg)) {
            pendingMap.remove(userId);
            AssistantChatResponse response = new AssistantChatResponse("好的，已取消这笔转账。");
            attachTrace(response, "transfer", "transfer_validation", "cancelled", "high", "",
                    List.of("检测到待确认转账", "用户取消转账"));
            return Result.success(response);
        }

        return null;
    }

    /**
     * 处理转账
     */
    private Result handleTransfer(Long userId, JsonNode parsed, String rawMsg) {
        String to = parsed.path("toAccountNo").asText("").trim();
        String payeeAlias = parsed.path("payeeAlias").asText("").trim();
        String amountStr = parsed.path("amount").asText("");
        String desc = parsed.path("description").asText("").trim();
        String repeatMode = parsed.path("repeatMode").asText("").trim();
        Long payeeId = null;
        String resolvedAlias = "无";
        String referenceSummary = "";

        if (to.isBlank() && payeeAlias.isBlank()) {
            payeeAlias = extractPayeeAliasFromMessage(rawMsg);
        }
        if (desc.isBlank()) {
            desc = extractDescriptionFromMessage(rawMsg);
        }

        if (to.isBlank() && !payeeAlias.isBlank()) {
            List<PayeeContact> payees = payeeContactMapper.selectByUserIdAndAlias(userId, payeeAlias);
            if (payees == null || payees.isEmpty()) {
                AssistantChatResponse response = new AssistantChatResponse(
                        "未找到昵称为【" + payeeAlias + "】的收款人，请先到收款人管理新增，或直接提供银行卡号。"
                );
                attachTrace(response, "transfer", "payee_lookup", "need_more_info", "high", "",
                        List.of("识别为转账意图", "尝试按昵称匹配收款人", "未匹配到收款人"));
                return Result.success(response);
            }
            if (payees.size() > 1) {
                AssistantChatResponse response = new AssistantChatResponse(
                        "检测到多个昵称为【" + payeeAlias + "】的收款人，请使用更具体昵称或直接提供银行卡号。"
                );
                attachTrace(response, "transfer", "payee_lookup", "need_more_info", "medium", "",
                        List.of("识别为转账意图", "尝试按昵称匹配收款人", "匹配到多个收款人"));
                return Result.success(response);
            }
            PayeeContact targetPayee = payees.get(0);
            payeeId = targetPayee.getId();
            to = safe(targetPayee.getAccountNo());
            if (targetPayee.getAlias() != null && !targetPayee.getAlias().isBlank()) {
                resolvedAlias = targetPayee.getAlias();
            }
        }

        if (!to.isBlank()) {
            PayeeContact byAccount = payeeContactMapper.selectByUserIdAndAccountNo(userId, to);
            if (byAccount != null) {
                payeeId = byAccount.getId();
                if (byAccount.getAlias() != null && !byAccount.getAlias().isBlank()) {
                    resolvedAlias = byAccount.getAlias();
                } else {
                    resolvedAlias = "无";
                }
            }
        }

        if (to.isBlank()) {
            AssistantChatResponse response = new AssistantChatResponse(
                    "请提供完整的转账信息，例如：向6222000012345678转100元，备注午餐费，或给弟弟转300元。"
            );
            attachTrace(response, "transfer", "transfer_validation", "need_more_info", "high", "",
                    List.of("识别为转账意图", "缺少收款账号/昵称"));
            return Result.success(response);
        }

        BigDecimal amount = null;
        if (!amountStr.isBlank()) {
            try {
                amount = new BigDecimal(amountStr.trim());
            } catch (Exception e) {
                AssistantChatResponse response = new AssistantChatResponse("转账金额格式不正确，请输入数字金额，例如：300 或 300.00。");
                attachTrace(response, "transfer", "transfer_validation", "need_more_info", "high", "",
                        List.of("识别为转账意图", "金额格式解析失败"));
                return Result.success(response);
            }
        } else if (isRepeatByTimeReference(parsed, rawMsg, repeatMode)) {
            ReferenceRange referenceRange = resolveReferenceRange(parsed);
            if (referenceRange == null) {
                AssistantChatResponse response = new AssistantChatResponse("请补充参考时间，例如：和昨天一样、和前天一样、和上个月一样。");
                attachTrace(response, "transfer", "time_reference", "need_more_info", "medium", "",
                        List.of("识别为转账意图", "识别到按历史时间复用金额", "缺少可解析的参考时间范围"));
                return Result.success(response);
            }
            ReferenceTransfer referenceTransfer = resolveReferenceTransfer(userId, to, desc, rawMsg, referenceRange);
            if (referenceTransfer == null) {
                String promptMsg = desc.isBlank()
                        ? referenceRange.label() + "未找到该收款人的支出流水，无法自动确定金额，请补充转账金额。"
                        : referenceRange.label() + "未找到与【" + desc + "】相关的该收款人支出流水，无法自动确定金额，请补充转账金额。";
                AssistantChatResponse response = new AssistantChatResponse(promptMsg);
                attachTrace(response, "transfer", "time_reference", "need_more_info", "medium", referenceRange.label(),
                        List.of("识别为转账意图", "识别到“和时间参考一样”", "查询参考时间同收款人流水失败"));
                return Result.success(response);
            }
            amount = referenceTransfer.amount();
            if (desc.isBlank() && referenceTransfer.description() != null && !referenceTransfer.description().isBlank()) {
                desc = referenceTransfer.description();
            }
            referenceSummary = referenceTransfer.summary();
        } else {
            AssistantChatResponse response = new AssistantChatResponse(
                    "请提供转账金额，例如：给弟弟转300元，或说“给弟弟转午餐费，和前天一样”。"
            );
            attachTrace(response, "transfer", "transfer_validation", "need_more_info", "high", "",
                    List.of("识别为转账意图", "缺少金额"));
            return Result.success(response);
        }

        TransferRequestDTO dto = new TransferRequestDTO();
        dto.setToAccountNo(to);
        dto.setAmount(amount);
        dto.setDescription(desc);

        TransferValidateVO vo;
        if (payeeId != null) {
            PayeeTransferDTO payeeTransferDTO = new PayeeTransferDTO();
            payeeTransferDTO.setAmount(amount);
            payeeTransferDTO.setDescription(desc);
            Result validateResult = payeeService.validateTransfer(userId, payeeId, payeeTransferDTO);
            if (validateResult.getData() instanceof TransferValidateVO transferValidateVO) {
                vo = transferValidateVO;
            } else {
                vo = accountService.validateTransfer(userId, dto);
            }
        } else {
            vo = accountService.validateTransfer(userId, dto);
        }

        PendingTransfer p = new PendingTransfer();
        p.setPayeeId(payeeId);
        p.setPayeeAlias(resolvedAlias);
        p.setToAccountNo(to);
        p.setAmount(dto.getAmount());
        p.setDescription(desc);
        p.setCreatedAt(LocalDateTime.now());
        pendingMap.put(userId, p);

        String reply =
                "我将为你发起转账，请确认：\n" +
                        "付款账号：" + vo.getFromAccountNoMasked() + "\n" +
                        "收款账号：" + vo.getToAccountNoMasked() + "\n" +
                        "收款人昵称：" + resolvedAlias + "\n" +
                        (!referenceSummary.isBlank() ? "参考依据：" + referenceSummary + "\n" : "") +
                        "金额：" + vo.getAmount() + "，余额：" + vo.getBalance() + "\n" +
                        ((vo.getDescription() != null && !vo.getDescription().isBlank()) ? "备注：" + vo.getDescription() + "\n" : "") +
                        "回复【确认】执行，回复【取消】放弃。";

        AssistantChatResponse response = new AssistantChatResponse(reply);
        List<String> steps = new ArrayList<>();
        steps.add("识别为转账意图");
        steps.add("解析收款账号/昵称");
        if (!referenceSummary.isBlank()) {
            steps.add("识别到“和时间参考一样”并复用参考流水金额");
        }
        steps.add("完成转账前校验");
        steps.add("等待用户确认");
        attachTrace(response, "transfer", "transfer_validation", "awaiting_confirmation", "high", "",
                steps);
        return Result.success(response);
    }

    /**
     * 处理查流水
     */


    private Result handleTransactions(Long userId, JsonNode parsed) {
        String txnType = parsed.path("txnType").asText("");
        String limitStr = parsed.path("limit").asText("");
        String yearStr = parsed.path("year").asText("");
        String monthStr = parsed.path("month").asText("");
        String dayStr = parsed.path("day").asText("");
        String timeRange = parsed.path("timeRange").asText("");
        String rangeValueStr = parsed.path("rangeValue").asText("");
        boolean needMonth = parsed.path("needMonth").asBoolean(false);

        if (needMonth && !dayStr.isBlank() && monthStr.isBlank()) {
            PendingTransactionQuery pending = new PendingTransactionQuery();
            pending.setType(txnType);
            pending.setDay(Time.parseInteger(dayStr));
            pending.setYear(Time.parseInteger(yearStr));
            pending.setLimit(Time.parseInteger(limitStr));
            pending.setRawTimeText("day_without_month");
            pending.setCreatedAt(LocalDateTime.now());

            pendingTxnQueryMap.put(userId, pending);

            AssistantChatResponse response = new AssistantChatResponse(
                    "请问你要查询几月" + dayStr + "号的" + (txnType.isBlank() ? "" : txnType) + "记录？例如回复“1月”或“今年1月”。"
            );
            attachTrace(response, "transactions", "transaction_query", "awaiting_more_context", "high", timeRange,
                    List.of("识别为流水查询意图", "检测到日期缺月份", "等待用户补充月份"));
            return Result.success(response);
        }

        TransactionQueryDTO q = new TransactionQueryDTO();
        q.setPage(1);
        q.setSize(limitStr.isBlank() ? 10 : Time.parseInteger(limitStr));

        Account account = accountService.getAccountInfo(userId);
        if (account == null) {
            throw BusinessException.of(ErrorCode.ACCOUNT_NOT_FOUND, "账户不存在");
        }
        q.setAccountId(account.getId());

        if (!txnType.isBlank()) {
            q.setType(txnType);
        }

        Time.fillTimeRange(q, timeRange, yearStr, monthStr, dayStr, rangeValueStr);

        Result result = transactionService.getTransactions(q);
        return convertTransactionResultToChat(result, txnType, q);
    }



    private Result handlePendingTransactionQuery(Long userId, String msg) {
        PendingTransactionQuery pending = pendingTxnQueryMap.get(userId);
        if (pending == null) {
            return null;
        }
        if (isPendingExpired(pending.getCreatedAt())) {
            pendingTxnQueryMap.remove(userId);
            AssistantChatResponse response = new AssistantChatResponse("上一次补全月份的查询已超时，请重新描述你的查询需求。");
            attachTrace(response, "transactions", "transaction_query", "expired", "high", "",
                    List.of("检测到待补全流水查询", "会话超时自动失效"));
            return Result.success(response);
        }

        if (isCancel(msg)) {
            pendingTxnQueryMap.remove(userId);
            AssistantChatResponse response = new AssistantChatResponse("好的，已取消这次流水查询。");
            attachTrace(response, "transactions", "transaction_query", "cancelled", "high", "",
                    List.of("检测到待补全流水查询", "用户取消查询"));
            return Result.success(response);
        }

        JsonNode parsed = parseIntentByQwen(msg);

        int month = Time.extractMonth(parsed, msg);
        if (month <= 0 || month > 12) {
            AssistantChatResponse response = new AssistantChatResponse("请直接告诉我月份，例如“1月”或“今年1月”。");
            attachTrace(response, "transactions", "transaction_query", "need_more_info", "medium", "",
                    List.of("检测到待补全流水查询", "未识别出合法月份"));
            return Result.success(response);
        }

        pending.setMonth(month);
        if (pending.getYear() == null) {
            pending.setYear(java.time.LocalDate.now().getYear());
        }

        pendingTxnQueryMap.remove(userId);

        TransactionQueryDTO q = buildTransactionQueryFromPending(userId, pending);
        Result result = transactionService.getTransactions(q);
        return convertTransactionResultToChat(result, pending.getType(), q);
    }


    @SuppressWarnings("unchecked")
    private Result convertTransactionResultToChat(Result result, String txnType, TransactionQueryDTO q) {
        if (result == null || result.getData() == null) {
            AssistantChatResponse response = new AssistantChatResponse("未查询到相关交易记录。");
            attachTrace(response, "transactions", "transaction_query", "completed", "high", buildTimeRangeText(q),
                    List.of("识别为流水查询意图", "调用流水查询服务", "结果为空"));
            response.setExplain(buildTransactionExplain(q, txnType));
            return Result.success(response);
        }

        Object data = result.getData();
        if (!(data instanceof com.idea.entity.PageResult<?> pageResultRaw)) {
            AssistantChatResponse response = new AssistantChatResponse("未查询到相关交易记录。");
            attachTrace(response, "transactions", "transaction_query", "completed", "medium", buildTimeRangeText(q),
                    List.of("识别为流水查询意图", "流水结果格式异常"));
            response.setExplain(buildTransactionExplain(q, txnType));
            return Result.success(response);
        }

        com.idea.entity.PageResult<?> pageResult = (com.idea.entity.PageResult<?>) data;
        List<?> rows = pageResult.getList();

        if (rows == null || rows.isEmpty()) {
            AssistantChatResponse response = new AssistantChatResponse("未查询到相关交易记录。");
            attachTrace(response, "transactions", "transaction_query", "completed", "high", buildTimeRangeText(q),
                    List.of("识别为流水查询意图", "调用流水查询服务", "结果为空"));
            response.setExplain(buildTransactionExplain(q, txnType));
            return Result.success(response);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("为你查询到");
        if (txnType != null && !txnType.isBlank()) {
            sb.append(txnType);
        }
        sb.append("记录如下：\n");

        int index = 1;
        for (Object row : rows) {
            if (row instanceof com.idea.vo.TransactionVO vo) {
                sb.append(index++).append(". ")
                        .append("时间：").append(vo.getTradeTime()).append("，")
                        .append("类型：").append(vo.getType()).append("，")
                        .append("金额：").append(vo.getAmount()).append("，")
                        .append("对方账号：").append(maskAccountNo(vo.getCounterpartyAccountNo())).append("，")
                        .append("备注：").append(vo.getDescription() == null ? "" : vo.getDescription())
                        .append("\n");
            }
        }

        AssistantChatResponse response = new AssistantChatResponse(sb.toString());
        response.setExplain(buildTransactionExplain(q, txnType));
        attachTrace(response, "transactions", "transaction_query", "completed", "high", buildTimeRangeText(q),
                List.of("识别为流水查询意图", "调用流水查询服务", "格式化返回结果"));
        return Result.success(response);
    }


    private String maskAccountNo(String accountNo) {
        if (accountNo == null || accountNo.length() < 8) {
            return "****";
        }
        return accountNo.substring(0, 4) + "****" + accountNo.substring(accountNo.length() - 4);
    }

    private TransactionQueryDTO buildTransactionQueryFromPending(Long userId, PendingTransactionQuery pending) {
        TransactionQueryDTO q = new TransactionQueryDTO();
        q.setPage(1);
        q.setSize(pending.getLimit() == null ? 10 : pending.getLimit());

        Account account = accountService.getAccountInfo(userId);
        if (account == null) {
            throw BusinessException.of(ErrorCode.ACCOUNT_NOT_FOUND, "账户不存在");
        }
        q.setAccountId(account.getId());

        if (pending.getType() != null && !pending.getType().isBlank()) {
            q.setType(pending.getType());
        }

        if (pending.getMonth() == null || pending.getDay() == null) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "日期信息不完整，无法查询流水");
        }

        int year = pending.getYear() == null ? java.time.LocalDate.now().getYear() : pending.getYear();
        int month = pending.getMonth();
        int day = pending.getDay();

        java.time.LocalDate date = java.time.LocalDate.of(year, month, day);
        q.setStartTime(String.valueOf(date.atStartOfDay()));
        q.setEndTime(String.valueOf(date.atTime(23, 59, 59)));

        return q;
    }




    /**
     * 处理查余额 / 查账户信息
     */
    private Result handleBalance(Long userId) {
        Account account = accountService.getAccountInfo(userId);
        if (account == null) {
            throw BusinessException.of(ErrorCode.ACCOUNT_NOT_FOUND, "账户不存在");
        }

        String reply =
                "为你查询到账户信息如下：\n" +
                        "户名：" + safe(account.getAccountName()) + "\n" +
                        "账号：" + safe(account.getAccountNo()) + "\n" +
                        "账户类型：" + safe(account.getAccountType()) + "\n" +
                        "状态：" + safe(account.getStatus()) + "\n" +
                        "余额：¥ " + (account.getBalance() == null ? "0.00" : account.getBalance().toPlainString());

        AssistantChatResponse response = new AssistantChatResponse(reply);
        attachTrace(response, "balance", "account_info", "completed", "high", "",
                List.of("识别为余额/账户查询意图", "查询账户信息"));
        return Result.success(response);
    }

    private Result handleFaqOrFallback(Long userId, String msg) {
        FaqKnowledgeService.FaqSearchResult faqResult = faqKnowledgeService.search(msg);
        faqKnowledgeService.logQuery(userId, msg, faqResult);

        if (faqResult.isHit()) {
            FaqKnowledge faq = faqResult.getBest().getFaq();
            String reply = "根据知识库，" + safe(faq.getAnswer());
            AssistantChatResponse response = new AssistantChatResponse(reply);
            attachTrace(response, "faq", "faq_knowledge_base", "completed", "high", "",
                    List.of(
                            "进入 FAQ 知识库检索",
                            "命中问题：" + safe(faq.getStandardQuestion()),
                            "匹配得分：" + formatScorePercent(faqResult.getBest().getScore()) + "%",
                            "返回知识库标准答案"
                    ));
            return Result.success(response);
        }

        if (faqResult.isRecommend()) {
            StringBuilder reply = new StringBuilder("我还不能完全确定你的问题，可能想咨询：");
            int index = 1;
            for (FaqKnowledgeService.ScoredFaq item : faqResult.getRecommendations()) {
                FaqKnowledge faq = item.getFaq();
                reply.append("\n").append(index++).append(". ").append(safe(faq.getStandardQuestion()));
            }
            reply.append("\n你可以直接输入对应问题，或补充描述后再问一次。");

            AssistantChatResponse response = new AssistantChatResponse(reply.toString());
            attachTrace(response, "faq", "faq_knowledge_base", "recommend", "medium", "",
                    List.of(
                            "进入 FAQ 知识库检索",
                            "未达到直接回答阈值",
                            "返回相似问题推荐"
                    ));
            return Result.success(response);
        }

        AssistantChatResponse fallback = new AssistantChatResponse(fallbackChat(msg));
        List<String> steps = new ArrayList<>();
        steps.add("进入 FAQ 知识库检索");
        steps.add(faqResult.isKnowledgeUnavailable() ? "知识库表暂不可用，跳过 FAQ 检索" : "知识库未命中");
        steps.add("进入闲聊兜底");
        attachTrace(fallback, "other", "qwen_fallback", "completed", "medium", "", steps);
        return Result.success(fallback);
    }

    private JsonNode parseIntentByQwen(String userMsg) {
        String system = """
你是银行智能客服的“意图解析器”。请把用户话解析成严格 JSON，不要输出多余文字。
只允许这些 intent：
- transfer（转账）
- transactions（查流水）
- balance（查余额、查账户信息）
- statistics（账单统计、汇总、趋势分析）
- other（其它）

规则：
1. 用户表达“查余额”“查一下我的余额”“我的账户信息”“查询账户信息”等，intent 一律输出 balance。
2. 用户表达查交易记录、查流水、查收入记录、查支出记录等，intent 输出 transactions。
3. 用户表达“统计、汇总、分析、总共、多少笔、最大一笔、趋势、对比”等账单分析需求时，intent 输出 statistics。
3.1 若用户要求“按消费类别/支出种类/商户类型/场景标签”统计，当前系统不支持，请 intent 输出 other。
3.2 用户询问银行卡挂失、密码找回、验证码、手续费、转账到账时间、转账限额、防诈骗、陌生人要求转账、贷款材料、人工客服、网点等常见咨询问题时，intent 输出 other，交给知识库处理。
3.3 用户问“转账多久到账/跨行转账要多久/转账失败怎么办/转账限额是多少”属于咨询问题，不是发起转账，intent 输出 other。

4. 当 intent=transfer 时必须给：
   toAccountNo（明确说了银行卡号时填写，否则空字符串）
   payeeAlias（明确说了收款人昵称/备注时填写，否则空字符串）
   repeatMode（若表达“和昨天一样/和前天一样/和上个月一样/和那天一样/那笔一样”等按历史时间复用金额，输出 time_same，否则空字符串）
   amount（数字字符串），description（可空字符串）
4.1 用户说“给弟弟转300元，备注午餐费”，且未给银行卡号时：
   toAccountNo="", payeeAlias="弟弟", amount="300", description="午餐费", intent=transfer
4.2 用户说“给我弟弟转午餐费，和昨天一样”时：
   toAccountNo="", payeeAlias="弟弟", amount="", description="午餐费", repeatMode="time_same", intent=transfer
4.3 用户说“给我弟弟转晚餐费，和前天一样”时：
   toAccountNo="", payeeAlias="弟弟", amount="", description="晚餐费", repeatMode="time_same", intent=transfer
   并尽量提取 year/month/day/timeRange（用于后端按 Time 工具计算参考时间范围）
4.4 用户说“给我弟弟转午餐费，和上个月一样”时：
   toAccountNo="", payeeAlias="弟弟", amount="", description="午餐费", repeatMode="time_same", intent=transfer
   并提取 timeRange=month，month=上个月对应月份，year=对应年份

5. 当 intent=transactions 时尽量提取：
   txnType（收入/支出/空字符串）
   limit（条数，例如 3）
   year（年份，例如 2026）
   month（月份，例如 1）
   day（日期，例如 17）
   timeRange（latest/month/day/year/week/last_week/quarter/last_quarter/recent_days/recent_years/other）
   rangeValue（数字，例如 3、7、30）
   needMonth（true/false）

6. 当 intent=statistics 时尽量提取：
   txnType（收入/支出/空字符串）
   year（年份，例如 2026）
   month（月份，例如 1）
   day（日期，例如 17）
   timeRange（latest/month/day/year/week/last_week/quarter/last_quarter/recent_days/recent_years/other）
   rangeValue（数字，例如 3、7、30）
   metric（sum/count/max/trend）
   groupBy（none/day/month）
   comparePrevious（true/false）

7. “查最近3条支出记录” -> txnType=支出, limit=3, timeRange=latest, intent=transactions
8. “查今年1月的收入记录” -> txnType=收入, year=当前年, month=1, timeRange=month, intent=transactions
9. “查17号的收入记录” -> txnType=收入, day=17, month="", needMonth=true, intent=transactions
10. “查前几天的支出记录” -> txnType=支出, timeRange=recent_days, rangeValue=3, intent=transactions
11. “查最近几年的收入记录” -> txnType=收入, timeRange=recent_years, rangeValue=3, intent=transactions
12. “查今天的流水” -> txnType="", year=当前年, month=当前月, day=当前日, timeRange=day, needMonth=false, intent=transactions
13. 只有用户明确说了“17号”“20号”这种“几号”但没月份时，needMonth 才为 true。

14. “统计这个月总支出” -> intent=statistics, txnType=支出, metric=sum, timeRange=month, groupBy=none
15. “最近7天我花了多少钱” -> intent=statistics, txnType=支出, metric=sum, timeRange=recent_days, rangeValue=7, groupBy=none
16. “本月有多少笔收入” -> intent=statistics, txnType=收入, metric=count, timeRange=month, groupBy=none
17. “最近最大一笔支出” -> intent=statistics, txnType=支出, metric=max, timeRange=latest, groupBy=none
18. “统计今年每个月的支出” -> intent=statistics, txnType=支出, metric=trend, timeRange=recent_years, rangeValue=1, groupBy=month
19. “看近三个月收入趋势” -> intent=statistics, txnType=收入, metric=trend, groupBy=month

20. 当不是 transfer 时，toAccountNo、payeeAlias、repeatMode、amount、description 返回空字符串。
21. 当不是 transactions 时，limit、needMonth 可默认空字符串/false。
22. 当不是 statistics 时，metric、groupBy 返回空字符串，comparePrevious 返回 false。
""";


        String user = """
用户话：%s

按下面 JSON 模板输出（必须严格 JSON）：
{
  "intent":"transfer|transactions|balance|statistics|other",
  "toAccountNo":"",
  "payeeAlias":"",
  "repeatMode":"",
  "amount":"",
  "description":"",
  "txnType":"",
  "limit":"",
  "year":"",
  "month":"",
  "day":"",
  "timeRange":"",
  "rangeValue":"",
  "needMonth":false,
  "metric":"",
  "groupBy":"",
  "comparePrevious":false
}
""".formatted(userMsg);


        JsonNode raw = qwenClient.chat(List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", user)
        ));

        String content = raw.path("choices").get(0).path("message").path("content").asText("{}");
        content = sanitizeJsonContent(content);

        try {
            JsonNode node = objectMapper.readTree(content);
            if (containsUnsupportedScope(userMsg)) {
                return objectMapper.createObjectNode().put("intent", "other");
            }
            return node;
        } catch (Exception e) {
            log.warn("assistant parse intent failed, content={}", content, e);
            return objectMapper.createObjectNode().put("intent", "other");
        }
    }


    private String fallbackChat(String userMsg) {
        JsonNode raw = qwenClient.chat(List.of(
                Map.of("role", "system", "content", """
你是银行智能客服，回答要简短、清晰、偏业务。
仅可回答当前已支持能力：查余额、查流水、转账确认、收支统计、趋势和对比。
禁止声称支持“支出分类/品类/商户类型/消费场景标签分析”等未实现能力。
若用户问到未实现能力，请直接说明当前不支持，并建议改问已支持能力。
"""),
                Map.of("role", "user", "content", userMsg)
        ));
        String content = raw.path("choices").get(0).path("message").path("content").asText("我明白啦，你可以再说具体一点~");
        return sanitizeModelReply(content, "当前可支持：查余额、查流水、转账确认、收支统计与趋势分析。");
    }

    private Result handleEmotionSupport(String msg) {
        String emotionType = detectEmotionType(msg);
        if (emotionType.isBlank()) {
            return null;
        }
        String reply = switch (emotionType) {
            case "complaint" -> "我理解你现在可能对服务结果不满意。为了避免问题继续扩大，建议先保留相关交易记录或页面截图，我也可以继续帮你查询余额、流水或统计信息。若需要人工进一步处理，请拨打虚拟客服电话 "
                    + HUMAN_SERVICE_PHONE + " 联系人工客服。";
            case "angry" -> "我能理解你现在比较着急或生气。涉及账户和资金的问题建议先不要重复提交操作，我可以继续帮你查询余额、流水或核对转账状态。若需要人工介入，请拨打虚拟客服电话 "
                    + HUMAN_SERVICE_PHONE + " 联系人工客服。";
            default -> "我理解你现在可能有些担心。你可以先把问题说清楚，我会尽量帮你查询余额、流水、统计或转账确认信息。若需要人工协助，请拨打虚拟客服电话 "
                    + HUMAN_SERVICE_PHONE + " 联系人工客服。";
        };
        AssistantChatResponse response = new AssistantChatResponse(reply);
        attachTrace(response, "emotion_support", "rule_emotion", "completed", "medium", "",
                List.of("检测到异常情绪或投诉表达", "先进行安抚说明", "提示人工客服电话"));
        return Result.success(response);
    }

    private boolean isRepeatByTimeReference(JsonNode parsed, String rawMsg, String repeatMode) {
        if ("time_same".equalsIgnoreCase(repeatMode) || "yesterday_same".equalsIgnoreCase(repeatMode)) {
            return true;
        }
        if (!hasParsedReferenceTime(parsed)) {
            return false;
        }
        if (rawMsg == null || rawMsg.isBlank()) {
            return false;
        }
        return rawMsg.contains("一样")
                || rawMsg.contains("那笔")
                || rawMsg.contains("照旧")
                || rawMsg.contains("同上");
    }

    private boolean hasParsedReferenceTime(JsonNode parsed) {
        if (parsed == null) {
            return false;
        }
        String yearStr = parsed.path("year").asText("").trim();
        String monthStr = parsed.path("month").asText("").trim();
        String dayStr = parsed.path("day").asText("").trim();
        String timeRange = parsed.path("timeRange").asText("").trim();
        String rangeValue = parsed.path("rangeValue").asText("").trim();
        return !yearStr.isBlank()
                || !monthStr.isBlank()
                || !dayStr.isBlank()
                || (!timeRange.isBlank() && !"latest".equalsIgnoreCase(timeRange))
                || !rangeValue.isBlank();
    }

    private ReferenceRange resolveReferenceRange(JsonNode parsed) {
        String timeRange = parsed.path("timeRange").asText("").trim();
        String yearStr = parsed.path("year").asText("").trim();
        String monthStr = parsed.path("month").asText("").trim();
        String dayStr = parsed.path("day").asText("").trim();
        String rangeValueStr = parsed.path("rangeValue").asText("").trim();

        TransactionQueryDTO q = new TransactionQueryDTO();
        Time.fillTimeRange(q, timeRange, yearStr, monthStr, dayStr, rangeValueStr);

        String startTime = q.getStartTime();
        String endTime = q.getEndTime();
        if (startTime != null && !startTime.isBlank() && endTime != null && !endTime.isBlank()) {
            return new ReferenceRange(startTime, endTime, buildReferenceLabel(startTime, endTime, timeRange, rangeValueStr));
        }
        return null;
    }

    private String buildReferenceLabel(String startTime, String endTime, String timeRange, String rangeValue) {
        String startDate = extractDate(startTime);
        String endDate = extractDate(endTime);

        if (!startDate.isBlank() && startDate.equals(endDate)) {
            return startDate;
        }
        if ("month".equals(timeRange) && startDate.length() >= 7) {
            return startDate.substring(0, 7);
        }
        if ("recent_days".equals(timeRange) && rangeValue != null && !rangeValue.isBlank()) {
            return "近" + rangeValue + "天";
        }
        if ("recent_years".equals(timeRange) && rangeValue != null && !rangeValue.isBlank()) {
            return "近" + rangeValue + "年";
        }
        if (!startDate.isBlank() && !endDate.isBlank()) {
            return startDate + " 至 " + endDate;
        }
        if (!startDate.isBlank()) {
            return startDate;
        }
        return "参考时间";
    }

    private String extractDate(String dateTimeText) {
        if (dateTimeText == null) {
            return "";
        }
        String value = dateTimeText.trim();
        if (value.length() >= 10) {
            return value.substring(0, 10);
        }
        return "";
    }

    private ReferenceTransfer resolveReferenceTransfer(Long userId, String toAccountNo, String descriptionKeyword, String rawMsg, ReferenceRange range) {
        Account account = accountService.getAccountInfo(userId);
        if (account == null) {
            throw BusinessException.of(ErrorCode.ACCOUNT_NOT_FOUND, "账户不存在");
        }
        if (toAccountNo == null || toAccountNo.isBlank() || range == null) {
            return null;
        }

        TransactionRecord reference = null;
        List<String> keywordCandidates = buildDescriptionKeywordCandidates(descriptionKeyword, rawMsg);
        for (String keyword : keywordCandidates) {
            reference = transactionMapper.selectLatestExpenseByCounterpartyAndDescription(
                    account.getId(), toAccountNo, range.startTime(), range.endTime(), keyword
            );
            if (reference != null) {
                break;
            }
        }
        if (reference == null) {
            reference = transactionMapper.selectLatestExpenseByCounterparty(
                    account.getId(), toAccountNo, range.startTime(), range.endTime()
            );
        }
        if (reference == null || reference.getAmount() == null) {
            return null;
        }

        BigDecimal amount = reference.getAmount().abs();
        String desc = safe(reference.getDescription());
        String tradeTimeText = reference.getTradeTime() == null ? "" : reference.getTradeTime().toString();
        String summary = range.label();
        if (!tradeTimeText.isBlank()) {
            summary += " " + tradeTimeText;
        }
        if (!desc.isBlank()) {
            summary += " 备注“" + desc + "”";
        }
        summary += "，金额 " + amount.toPlainString();

        return new ReferenceTransfer(amount, desc, summary);
    }

    private List<String> buildDescriptionKeywordCandidates(String descriptionKeyword, String rawMsg) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        String normalized = normalizeKeyword(descriptionKeyword);
        if (!normalized.isBlank()) {
            candidates.add(normalized);
        }

        if (candidates.isEmpty()) {
            String inferred = normalizeKeyword(extractDescriptionFromMessage(rawMsg));
            if (!inferred.isBlank()) {
                candidates.add(inferred);
            }
        }

        if (!candidates.isEmpty()) {
            String first = candidates.iterator().next();
            String compact = first.replace(" ", "");
            if (!compact.isBlank()) {
                candidates.add(compact);
            }
            String[] tokens = compact.split("[、/\\-\\s]+");
            for (String token : tokens) {
                String t = token.trim();
                if (t.length() >= 2) {
                    candidates.add(t);
                }
            }
        }

        return new ArrayList<>(candidates);
    }

    private String extractPayeeAliasFromMessage(String rawMsg) {
        if (rawMsg == null || rawMsg.isBlank()) {
            return "";
        }
        Matcher matcher = Pattern.compile("给(?:我)?([^\\d\\s，,。.!?；;：:]{1,12}?)(?:转账|转|打款|汇款)").matcher(rawMsg);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private String extractDescriptionFromMessage(String rawMsg) {
        if (rawMsg == null || rawMsg.isBlank()) {
            return "";
        }

        Matcher remarkMatcher = Pattern.compile("备注[:：]?\\s*([^，,。.!?；;]+)").matcher(rawMsg);
        if (remarkMatcher.find()) {
            return normalizeKeyword(remarkMatcher.group(1));
        }

        Matcher matcher = Pattern.compile("(?:转账|转|打款|汇款)\\s*([^，,。.!?；;]*)").matcher(rawMsg);
        if (!matcher.find()) {
            return "";
        }

        String candidate = matcher.group(1).trim();
        candidate = candidate.replaceFirst("^\\d+(?:\\.\\d+)?(?:元|块)?", "").trim();
        candidate = candidate.replaceFirst("\\d+(?:\\.\\d+)?(?:元|块)$", "").trim();
        return normalizeKeyword(candidate);
    }

    private String normalizeKeyword(String text) {
        String keyword = safe(text).trim();
        if (keyword.isBlank()) {
            return "";
        }
        keyword = keyword
                .replace("备注", "")
                .trim();
        // 去掉尾部“和X一样/跟X一样/按X一样”等时间复用短语，避免影响备注关键词检索
        keyword = keyword.replaceAll("(?:，|,)?\\s*(?:和|跟|按)[^，,。.!?；;]{0,20}一样\\s*$", "").trim();
        keyword = keyword
                .replaceAll("^[，,。.!?；;：:\\s]+", "")
                .replaceAll("[，,。.!?；;：:\\s]+$", "")
                .trim();
        return keyword;
    }

    private boolean isConfirm(String msg) {
        return msg.equals("确认") || msg.equalsIgnoreCase("confirm") || msg.contains("确认转账");
    }

    private boolean isCancel(String msg) {
        return msg.equals("取消") || msg.equalsIgnoreCase("cancel") || msg.contains("不转了");
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String formatScorePercent(double score) {
        return BigDecimal.valueOf(score * 100).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String detectEmotionType(String msg) {
        if (msg == null || msg.isBlank()) {
            return "";
        }
        String text = msg.toLowerCase();
        String[] complaintKeywords = new String[]{"我要投诉", "投诉你们", "客服投诉", "服务太差", "乱扣", "扣错"};
        for (String keyword : complaintKeywords) {
            if (text.contains(keyword.toLowerCase())) {
                return "complaint";
            }
        }
        String[] angryKeywords = new String[]{"生气", "气死", "愤怒", "火大", "烦死", "垃圾", "太差了", "崩溃"};
        for (String keyword : angryKeywords) {
            if (text.contains(keyword.toLowerCase())) {
                return "angry";
            }
        }
        String[] anxiousKeywords = new String[]{"很着急", "急死", "焦虑", "担心", "害怕", "慌"};
        for (String keyword : anxiousKeywords) {
            if (text.contains(keyword.toLowerCase())) {
                return "anxious";
            }
        }
        return "";
    }

    private void sendEvent(SseEmitter emitter, String event, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(event).data(data));
    }

    private void attachTrace(AssistantChatResponse response, String intent, String tool, String status,
                             String confidence, String timeRange, List<String> steps) {
        response.setTrace(new AssistantChatResponse.TraceInfo(
                intent,
                tool,
                status,
                confidence,
                safe(timeRange),
                steps
        ));
    }

    private boolean isPendingExpired(LocalDateTime createdAt) {
        if (createdAt == null) {
            return false;
        }
        return createdAt.plus(PENDING_TTL).isBefore(LocalDateTime.now());
    }

    private String sanitizeJsonContent(String rawContent) {
        if (rawContent == null) {
            return "{}";
        }
        String content = rawContent.trim();
        if (content.startsWith("```")) {
            content = content.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            content = content.substring(start, end + 1);
        }
        return content;
    }

    private Map<String, Object> buildTransactionExplain(TransactionQueryDTO q, String txnType) {
        return Map.of(
                "txnType", safe(txnType),
                "startTime", safe(q.getStartTime()),
                "endTime", safe(q.getEndTime()),
                "limit", q.getSize() == null ? 0 : q.getSize()
        );
    }

    private String buildTimeRangeText(TransactionQueryDTO q) {
        return safe(q.getStartTime()) + " ~ " + safe(q.getEndTime());
    }

    private StatisticsQueryDTO buildBriefQuery(Long accountId, String txnType, String metric, String period) {
        StatisticsQueryDTO q = new StatisticsQueryDTO();
        q.setAccountId(accountId);
        q.setTxnType(txnType);
        q.setMetric(metric);
        q.setGroupBy("trend".equals(metric) ? "day" : "none");
        if ("month".equals(period)) {
            Time.fillTimeRange(q, "month", "", "", "", "");
        } else {
            Time.fillTimeRange(q, "week", "", "", "", "");
        }
        return q;
    }

    private List<Map<String, String>> detectExpenseAnomaly(Long accountId, BigDecimal currentExpense, String period) {
        List<Map<String, String>> anomalies = new ArrayList<>();
        StatisticsQueryDTO baseline = new StatisticsQueryDTO();
        baseline.setAccountId(accountId);
        baseline.setTxnType("支出");
        baseline.setMetric("sum");
        baseline.setGroupBy("none");
        Time.fillTimeRange(baseline, "recent_days", "", "", "", "28");
        StatisticsResultVO baselineVo = statisticsService.statistics(baseline);
        BigDecimal baselineExpense = baselineVo.getValue() == null ? BigDecimal.ZERO : baselineVo.getValue();
        BigDecimal avgPerWeek = baselineExpense.divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);
        if (avgPerWeek.compareTo(BigDecimal.ZERO) > 0
                && currentExpense.compareTo(avgPerWeek.multiply(BigDecimal.valueOf(1.5))) > 0) {
            anomalies.add(Map.of("level", "warning", "message", "异常提醒：当前周期总支出显著高于近4周周均值。"));
        }

        StatisticsQueryDTO baselineP90Query = new StatisticsQueryDTO();
        baselineP90Query.setAccountId(accountId);
        Time.fillTimeRange(baselineP90Query, "recent_days", "", "", "", "30");
        List<BigDecimal> expenseAmountList = transactionMapper.listExpenseAmounts(baselineP90Query);
        BigDecimal p90 = calculatePercentile(expenseAmountList, 0.9);
        if (p90.compareTo(BigDecimal.ZERO) > 0) {
            StatisticsQueryDTO currentMaxQuery = new StatisticsQueryDTO();
            currentMaxQuery.setAccountId(accountId);
            currentMaxQuery.setTxnType("支出");
            currentMaxQuery.setMetric("max");
            currentMaxQuery.setGroupBy("none");
            if ("month".equals(period)) {
                Time.fillTimeRange(currentMaxQuery, "month", "", "", "", "");
            } else {
                Time.fillTimeRange(currentMaxQuery, "week", "", "", "", "");
            }
            StatisticsResultVO maxVo = statisticsService.statistics(currentMaxQuery);
            BigDecimal currentMax = maxVo.getValue() == null ? BigDecimal.ZERO : maxVo.getValue();
            if (currentMax.compareTo(p90) > 0) {
                anomalies.add(Map.of(
                        "level", "warning",
                        "message", "异常提醒：当前周期出现超大单笔支出，已超过近30天P90阈值。"
                ));
            }
        }
        return anomalies;
    }

    private BigDecimal calculatePercentile(List<BigDecimal> list, double percentile) {
        if (list == null || list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        int n = list.size();
        int index = (int) Math.ceil(percentile * n) - 1;
        index = Math.max(0, Math.min(index, n - 1));
        return list.get(index) == null ? BigDecimal.ZERO : list.get(index);
    }

    private Map<String, Object> buildBriefComparison(Long accountId, String period, BigDecimal currentIncome, BigDecimal currentExpense) {
        StatisticsQueryDTO prevIncomeQuery = new StatisticsQueryDTO();
        StatisticsQueryDTO prevExpenseQuery = new StatisticsQueryDTO();
        prevIncomeQuery.setAccountId(accountId);
        prevIncomeQuery.setTxnType("收入");
        prevIncomeQuery.setMetric("sum");
        prevIncomeQuery.setGroupBy("none");
        prevExpenseQuery.setAccountId(accountId);
        prevExpenseQuery.setTxnType("支出");
        prevExpenseQuery.setMetric("sum");
        prevExpenseQuery.setGroupBy("none");

        if ("month".equals(period)) {
            Time.fillTimeRange(prevIncomeQuery, "month", "", "", "", "");
            Time.fillTimeRange(prevExpenseQuery, "month", "", "", "", "");
            shiftQueryToPreviousMonth(prevIncomeQuery);
            shiftQueryToPreviousMonth(prevExpenseQuery);
        } else {
            Time.fillTimeRange(prevIncomeQuery, "last_week", "", "", "", "");
            Time.fillTimeRange(prevExpenseQuery, "last_week", "", "", "", "");
        }

        BigDecimal previousIncome = safeAmount(statisticsService.statistics(prevIncomeQuery).getValue());
        BigDecimal previousExpense = safeAmount(statisticsService.statistics(prevExpenseQuery).getValue());
        BigDecimal currentNet = currentIncome.subtract(currentExpense);
        BigDecimal previousNet = previousIncome.subtract(previousExpense);
        Map<String, Object> deltaPct = new HashMap<>();
        deltaPct.put("income", calculateDeltaPercent(currentIncome, previousIncome));
        deltaPct.put("expense", calculateDeltaPercent(currentExpense, previousExpense));
        deltaPct.put("net", calculateDeltaPercent(currentNet, previousNet));
        return Map.of(
                "currentPeriodLabel", "month".equals(period) ? "本月" : "本周",
                "previousPeriodLabel", "month".equals(period) ? "上月" : "上周",
                "current", Map.of(
                        "income", currentIncome.toPlainString(),
                        "expense", currentExpense.toPlainString(),
                        "net", currentNet.toPlainString()
                ),
                "previous", Map.of(
                        "income", previousIncome.toPlainString(),
                        "expense", previousExpense.toPlainString(),
                        "net", previousNet.toPlainString()
                ),
                "deltaPct", deltaPct
        );
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Double calculateDeltaPercent(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal delta = current.subtract(previous)
                .divide(previous.abs(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return delta.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private void shiftQueryToPreviousMonth(StatisticsQueryDTO query) {
        if (query.getStartTime() == null || query.getEndTime() == null) {
            return;
        }
        LocalDateTime start = LocalDateTime.parse(query.getStartTime().replace(" ", "T"));
        LocalDateTime end = LocalDateTime.parse(query.getEndTime().replace(" ", "T"));
        query.setStartTime(start.minusMonths(1).toString());
        query.setEndTime(end.minusMonths(1).toString());
    }

    private List<String> generateBriefSuggestions(String period, BigDecimal income, BigDecimal expense, BigDecimal net,
                                                  List<StatisticsPointVO> trendList) {
        if (!"month".equals(period)) {
            return List.of();
        }
        String facts = "周期=月报\n收入=" + income.toPlainString() + "\n支出=" + expense.toPlainString()
                + "\n净流入=" + net.toPlainString() + "\n趋势点数=" + (trendList == null ? 0 : trendList.size());
        try {
            JsonNode raw = qwenClient.chat(List.of(
                    Map.of("role", "system", "content", """
你是银行助手。只基于给定事实输出1-2条建议，简短可执行。
禁止输出未实现能力（消费分类、品类、商户类型、标签）。
禁止让用户“进一步提供分类信息”。
每条建议不超过18字。
"""),
                    Map.of("role", "user", "content", facts)
            ));
            String text = raw.path("choices").get(0).path("message").path("content").asText("").trim();
            text = sanitizeModelReply(text, "");
            if (text.isBlank()) {
                return List.of("建议控制非必要消费", "建议保持稳定结余");
            }
            String[] lines = text.split("\\n");
            List<String> result = new ArrayList<>();
            for (String line : lines) {
                String cleaned = line.replaceFirst("^[-*\\d.\\s]+", "").trim();
                if (!cleaned.isBlank()) {
                    result.add(cleaned);
                }
                if (result.size() >= 2) {
                    break;
                }
            }
            return result.isEmpty() ? List.of("建议控制非必要消费", "建议保持稳定结余") : result;
        } catch (Exception e) {
            log.warn("assistant generate brief suggestions fallback", e);
            return List.of("建议控制非必要消费", "建议保持稳定结余");
        }
    }

    private String sanitizeModelReply(String content, String fallback) {
        if (content == null || content.isBlank()) {
            return fallback;
        }
        String lowered = content.toLowerCase();
        String[] banned = new String[]{"支出种类", "消费类别", "商户类型", "场景标签", "分类统计"};
        for (String keyword : banned) {
            if (lowered.contains(keyword.toLowerCase())) {
                return fallback.isBlank() ? "当前仅支持收入/支出、时间范围、总额、笔数、最大值和趋势分析。" : fallback;
            }
        }
        return content;
    }

    private boolean containsUnsupportedScope(String msg) {
        if (msg == null) {
            return false;
        }
        return msg.contains("分类") || msg.contains("品类") || msg.contains("商户类型") || msg.contains("场景标签");
    }

    private record StatisticsInsight(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            StatisticsPointVO peakIncomeDay,
            StatisticsPointVO peakExpenseDay,
            List<StatisticsPointVO> topIncomeCounterparty,
            List<StatisticsPointVO> topExpenseCounterparty,
            StatisticsPointVO maxIncomeTxn,
            StatisticsPointVO maxExpenseTxn,
            Long incomeCount,
            Long expenseCount,
            List<String> anomalySignals
    ) {
    }

    private record ReferenceRange(String startTime, String endTime, String label) {
    }

    private record ReferenceTransfer(BigDecimal amount, String description, String summary) {
    }
}
