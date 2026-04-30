package com.idea.service.impl;

import com.idea.dto.StatisticsQueryDTO;
import com.idea.mapper.TransactionMapper;
import com.idea.vo.StatisticsPointVO;
import com.idea.vo.StatisticsResultVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    @Test
    void statisticsShouldReturnSumAndSummary() {
        when(transactionMapper.sumAmount(any(StatisticsQueryDTO.class))).thenReturn(new BigDecimal("1234.50"));

        StatisticsQueryDTO query = query("支出", "sum", "none");
        StatisticsResultVO result = statisticsService.statistics(query);

        assertEquals(new BigDecimal("1234.50"), result.getValue());
        assertEquals("支出总金额统计", result.getTitle());
        assertTrue(result.getSummary().contains("1234.50"));
    }

    @Test
    void statisticsShouldReturnTrendList() {
        StatisticsPointVO point = new StatisticsPointVO();
        point.setLabel("2026-04-01");
        point.setValue(new BigDecimal("88.00"));
        when(transactionMapper.statisticsTrend(any(StatisticsQueryDTO.class))).thenReturn(List.of(point));

        StatisticsQueryDTO query = query("支出", "trend", "day");
        StatisticsResultVO result = statisticsService.statistics(query);

        assertEquals("趋势统计", result.getTitle().substring(2));
        assertEquals(1, result.getTrendList().size());
        assertTrue(result.getSummary().contains("2026-04-01"));
    }

    @Test
    void statisticsShouldHandleEmptyMaxResult() {
        when(transactionMapper.maxTransaction(any(StatisticsQueryDTO.class))).thenReturn(null);

        StatisticsQueryDTO query = query("收入", "max", "none");
        StatisticsResultVO result = statisticsService.statistics(query);

        assertEquals(BigDecimal.ZERO, result.getValue());
        assertEquals("未查询到相关交易记录。", result.getSummary());
    }

    private StatisticsQueryDTO query(String txnType, String metric, String groupBy) {
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        query.setAccountId(1L);
        query.setTxnType(txnType);
        query.setMetric(metric);
        query.setGroupBy(groupBy);
        query.setStartTime("2026-04-01T00:00");
        query.setEndTime("2026-04-30T23:59:59");
        return query;
    }
}
