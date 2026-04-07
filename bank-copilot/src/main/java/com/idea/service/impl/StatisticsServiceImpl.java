package com.idea.service.impl;

import com.idea.dto.StatisticsQueryDTO;
import com.idea.mapper.TransactionMapper;
import com.idea.service.StatisticsService;
import com.idea.vo.StatisticsPointVO;
import com.idea.vo.StatisticsResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final TransactionMapper transactionMapper;

    @Override
    public StatisticsResultVO statistics(StatisticsQueryDTO query) {
        StatisticsResultVO vo = new StatisticsResultVO();
        vo.setMetric(query.getMetric());
        vo.setTxnType(query.getTxnType());

        switch (query.getMetric()) {
            case "sum":
                BigDecimal sum = transactionMapper.sumAmount(query);
                vo.setValue(sum == null ? BigDecimal.ZERO : sum);
                vo.setTitle(buildTitle(query, "总金额统计"));
                vo.setSummary(buildSumSummary(query, vo.getValue()));
                
                // 如果没有指定查收入还是支出（说明看的是整体汇总），附加上三张图表所需的数据
                if (query.getTxnType() == null || query.getTxnType().isBlank()) {
                    List<StatisticsPointVO> bar = transactionMapper.sumIncomeAndExpense(query);
                    vo.setIncomeExpenseBar(bar);
                    
                    StatisticsQueryDTO incomeQ = cloneQuery(query);
                    incomeQ.setTxnType("收入");
                    vo.setIncomePie(transactionMapper.statisticsByCounterparty(incomeQ));
                    
                    StatisticsQueryDTO expenseQ = cloneQuery(query);
                    expenseQ.setTxnType("支出");
                    vo.setExpensePie(transactionMapper.statisticsByCounterparty(expenseQ));
                }

                return vo;

            case "count":
                Long count = transactionMapper.countByStatistics(query);
                vo.setCount(count == null ? 0L : count);
                vo.setTitle(buildTitle(query, "笔数统计"));
                vo.setSummary(buildCountSummary(query, vo.getCount()));
                return vo;

            case "max":
                StatisticsPointVO maxPoint = transactionMapper.maxTransaction(query);
                if (maxPoint == null) {
                    vo.setValue(BigDecimal.ZERO);
                    vo.setSummary("未查询到相关交易记录。");
                } else {
                    vo.setValue(maxPoint.getValue() == null ? BigDecimal.ZERO : maxPoint.getValue());
                    vo.setTrendList(List.of(maxPoint));
                    vo.setSummary(buildMaxSummary(query, maxPoint));
                }
                vo.setTitle(buildTitle(query, "最大一笔统计"));
                return vo;

            case "trend":
                List<StatisticsPointVO> trendList = transactionMapper.statisticsTrend(query);
                vo.setTrendList(trendList);
                vo.setTitle(buildTitle(query, "趋势统计"));
                vo.setSummary(buildTrendSummary(query, trendList));
                return vo;

            default:
                vo.setTitle("未知统计类型");
                vo.setSummary("暂不支持该统计类型。");
                return vo;
        }
    }

    private String buildTitle(StatisticsQueryDTO query, String suffix) {
        String typeText = (query.getTxnType() == null || query.getTxnType().isBlank()) ? "全部" : query.getTxnType();
        return typeText + suffix;
    }

    private String buildSumSummary(StatisticsQueryDTO query, BigDecimal value) {
        String typeText = (query.getTxnType() == null || query.getTxnType().isBlank()) ? "交易" : query.getTxnType();
        return "统计完成：你的" + typeText + "总金额为 " + value.toPlainString() + " 元。";
    }

    private String buildCountSummary(StatisticsQueryDTO query, Long count) {
        String typeText = (query.getTxnType() == null || query.getTxnType().isBlank()) ? "交易" : query.getTxnType();
        return "统计完成：你的" + typeText + "共有 " + count + " 笔。";
    }

    private String buildMaxSummary(StatisticsQueryDTO query, StatisticsPointVO point) {
        String typeText = (query.getTxnType() == null || query.getTxnType().isBlank()) ? "交易" : query.getTxnType();
        return "统计完成：你最近最大一笔" + typeText + "为 "
                + (point.getValue() == null ? "0" : point.getValue().toPlainString())
                + " 元，发生时间为 " + (point.getLabel() == null ? "" : point.getLabel()) + "。";
    }

    private String buildTrendSummary(StatisticsQueryDTO query, List<StatisticsPointVO> trendList) {
        String typeText = (query.getTxnType() == null || query.getTxnType().isBlank()) ? "交易" : query.getTxnType();
        if (trendList == null || trendList.isEmpty()) {
            return "未查询到相关" + typeText + "趋势数据。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("统计完成：你的").append(typeText).append("趋势如下：\n");
        for (StatisticsPointVO point : trendList) {
            sb.append(point.getLabel())
                    .append("：")
                    .append(point.getValue() == null ? "0" : point.getValue().toPlainString())
                    .append(" 元\n");
        }
        return sb.toString();
    }

    private StatisticsQueryDTO cloneQuery(StatisticsQueryDTO q) {
        StatisticsQueryDTO dto = new StatisticsQueryDTO();
        dto.setAccountId(q.getAccountId());
        dto.setAccountNo(q.getAccountNo());
        dto.setStartTime(q.getStartTime());
        dto.setEndTime(q.getEndTime());
        dto.setMetric(q.getMetric());
        dto.setTxnType(q.getTxnType());
        dto.setGroupBy(q.getGroupBy());
        dto.setComparePrevious(q.getComparePrevious());
        return dto;
    }
}
