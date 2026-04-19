package com.idea.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idea.assistant.PendingTransfer;
import com.idea.common.ErrorCode;
import com.idea.dto.*;
import com.idea.entity.Account;
import com.idea.entity.Result;
import com.idea.exception.BusinessException;
import com.idea.llm.QwenClient;
import com.idea.service.impl.AccountServiceImpl;
import com.idea.service.impl.TransactionServiceImpl;
import com.idea.vo.AssistantChatResponse;
import com.idea.vo.StatisticsPointVO;
import com.idea.vo.StatisticsResultVO;
import com.idea.vo.TransferValidateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.idea.utils.Time;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AssistantService {

    private final QwenClient qwenClient;
    private final AccountServiceImpl accountService;
    private final TransactionServiceImpl transactionService;
    private final StatisticsService statisticsService;


    private final Map<Long, PendingTransfer> pendingMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, PendingTransactionQuery> pendingTxnQueryMap = new ConcurrentHashMap<>();

    public Result chat(Long userId, AssistantChatRequest req) {
        String msg = req.getMessage() == null ? "" : req.getMessage().trim();
        if (msg.isEmpty()) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "message 不能为空");
        }

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

        // 3. 解析意图
        JsonNode parsed = parseIntentByQwen(msg);
        parsed = Time.normalizeTimeByText(msg, parsed);
        String intent = parsed.path("intent").asText("");

        // 4. 分发处理
        switch (intent) {
            case "transfer":
                return handleTransfer(userId, parsed);
            case "transactions":
                return handleTransactions(userId, parsed);
            case "balance":
                return handleBalance(userId);
            case "statistics":
                return handleStatistics(userId, parsed);
            default:
                return Result.success(new AssistantChatResponse(fallbackChat(msg)));
        }

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
            return Result.success(new AssistantChatResponse(
                    "你可以这样说：统计这个月总支出、最近7天我花了多少钱、本月有多少笔收入、统计今年每个月的支出。"
            ));
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
        }

        return Result.success(response);
    }


    //分析型总结功能
    private String summarizeStatisticsByQwen(StatisticsQueryDTO q, StatisticsResultVO vo) {
        try {
            String facts = buildStatisticsFacts(q, vo);

            JsonNode raw = qwenClient.chat(List.of(
                    Map.of(
                            "role", "system",
                            "content", """
你是银行智能分析助手。
你的任务是：基于系统已经计算好的真实统计结果，生成简短、清晰、有业务感的分析型总结。

要求：
1. 只能基于给定事实总结，禁止编造数据。
2. 语气自然、专业、简洁，像银行智能助理。
3. 控制在 2~4 句。
4. 若数据为空或为 0，要明确说明未查询到明显记录或金额很少。
5. 如果是趋势数据，可以简单指出“整体变化情况”，但不能编造未提供的涨跌幅。
6. 仅进行总结分析即可，不要提示用户进一步分析！
7. 不要输出 JSON，不要解释过程，直接输出给用户的话。
"""
                    ),
                    Map.of(
                            "role", "user",
                            "content", facts
                    )
            ));

            String content = raw.path("choices").get(0).path("message").path("content").asText("").trim();
            if (content == null || content.isBlank()) {
                return vo.getSummary();
            }
            return content;
        } catch (Exception e) {
            return vo.getSummary();
        }
    }

    //数据整理
    private String buildStatisticsFacts(StatisticsQueryDTO q, StatisticsResultVO vo) {
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

        sb.append("系统规则总结：").append(safe(vo.getSummary())).append("\n");
        return sb.toString();
    }



    /**
     * 处理待确认转账
     */
    private Result handlePendingTransfer(Long userId, String msg) {
        PendingTransfer pending = pendingMap.get(userId);
        if (pending == null) {
            return null;
        }

        if (isConfirm(msg)) {
            TransferRequestDTO dto = new TransferRequestDTO();
            dto.setToAccountNo(pending.getToAccountNo());
            dto.setAmount(pending.getAmount());
            dto.setDescription(pending.getDescription());

            accountService.transfer(userId, dto);
            pendingMap.remove(userId);
            return Result.success(new AssistantChatResponse("已确认✅ 转账成功。"));
        }

        if (isCancel(msg)) {
            pendingMap.remove(userId);
            return Result.success(new AssistantChatResponse("好的，已取消这笔转账。"));
        }

        return null;
    }

    /**
     * 处理转账
     */
    private Result handleTransfer(Long userId, JsonNode parsed) {
        String to = parsed.path("toAccountNo").asText("");
        String amountStr = parsed.path("amount").asText("");
        String desc = parsed.path("description").asText("");

        if (to.isBlank() || amountStr.isBlank()) {
            return Result.success(new AssistantChatResponse("请提供完整的转账信息，例如：向6222000012345678转100元，备注午餐费。"));
        }

        TransferRequestDTO dto = new TransferRequestDTO();
        dto.setToAccountNo(to);
        dto.setAmount(new BigDecimal(amountStr));
        dto.setDescription(desc);

        // 校验但不执行
        TransferValidateVO vo = accountService.validateTransfer(userId, dto);

        PendingTransfer p = new PendingTransfer();
        p.setToAccountNo(to);
        p.setAmount(dto.getAmount());
        p.setDescription(desc);
        p.setCreatedAt(LocalDateTime.now());
        pendingMap.put(userId, p);

        String reply =
                "我将为你发起转账，请确认：\n" +
                        "付款账号：" + vo.getFromAccountNoMasked() + "\n" +
                        "收款账号：" + vo.getToAccountNoMasked() + "\n" +
                        "金额：" + vo.getAmount() + "，余额：" + vo.getBalance() + "\n" +
                        ((vo.getDescription() != null && !vo.getDescription().isBlank()) ? "备注：" + vo.getDescription() + "\n" : "") +
                        "回复【确认】执行，回复【取消】放弃。";

        return Result.success(new AssistantChatResponse(reply));
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

            return Result.success(new AssistantChatResponse(
                    "请问你要查询几月" + dayStr + "号的" + (txnType.isBlank() ? "" : txnType) + "记录？例如回复“1月”或“今年1月”。"
            ));
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

        if (isCancel(msg)) {
            pendingTxnQueryMap.remove(userId);
            return Result.success(new AssistantChatResponse("好的，已取消这次流水查询。"));
        }

        JsonNode parsed = parseIntentByQwen(msg);

        int month = Time.extractMonth(parsed, msg);
        if (month <= 0 || month > 12) {
            return Result.success(new AssistantChatResponse("请直接告诉我月份，例如“1月”或“今年1月”。"));
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
            return Result.success(new AssistantChatResponse("未查询到相关交易记录。"));
        }

        Object data = result.getData();
        if (!(data instanceof com.idea.entity.PageResult<?> pageResultRaw)) {
            return Result.success(new AssistantChatResponse("未查询到相关交易记录。"));
        }

        com.idea.entity.PageResult<?> pageResult = (com.idea.entity.PageResult<?>) data;
        List<?> rows = pageResult.getList();

        if (rows == null || rows.isEmpty()) {
            return Result.success(new AssistantChatResponse("未查询到相关交易记录。"));
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

        return Result.success(new AssistantChatResponse(sb.toString()));
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

        return Result.success(new AssistantChatResponse(reply));
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

4. 当 intent=transfer 时必须给：
   toAccountNo（字符串），amount（数字字符串），description（可空字符串）

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

20. 当不是 transfer 时，toAccountNo、amount、description 返回空字符串。
21. 当不是 transactions 时，limit、needMonth 可默认空字符串/false。
22. 当不是 statistics 时，metric、groupBy 返回空字符串，comparePrevious 返回 false。
""";


        String user = """
用户话：%s

按下面 JSON 模板输出（必须严格 JSON）：
{
  "intent":"transfer|transactions|balance|statistics|other",
  "toAccountNo":"",
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

        try {
            return objectMapper.readTree(content);
        } catch (Exception e) {
            return objectMapper.createObjectNode().put("intent", "other");
        }
    }


    private String fallbackChat(String userMsg) {
        JsonNode raw = qwenClient.chat(List.of(
                Map.of("role", "system", "content", "你是银行智能客服，回答要简短、清晰、偏业务。"),
                Map.of("role", "user", "content", userMsg)
        ));
        return raw.path("choices").get(0).path("message").path("content").asText("我明白啦，你可以再说具体一点~");
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
}
