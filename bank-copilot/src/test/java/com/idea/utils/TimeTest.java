package com.idea.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idea.dto.StatisticsQueryDTO;
import com.idea.dto.TransactionQueryDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void fillMonthRangeShouldUseWholeMonth() {
        TransactionQueryDTO query = new TransactionQueryDTO();

        Time.fillTimeRange(query, "month", "2026", "1", "", "");

        assertEquals("2026-01-01T00:00", query.getStartTime());
        assertEquals("2026-01-31T23:59:59", query.getEndTime());
    }

    @Test
    void fillRecentDaysShouldSetStartAndEnd() {
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        LocalDate today = LocalDate.now();

        Time.fillTimeRange(query, "recent_days", "", "", "", "7");

        assertEquals(String.valueOf(today.minusDays(6).atStartOfDay()), query.getStartTime());
        assertEquals(String.valueOf(today.atTime(23, 59, 59)), query.getEndTime());
        assertEquals(7, query.getRangeValue());
    }

    @Test
    void normalizeTimeByTextShouldCorrectYesterday() {
        ObjectNode parsed = objectMapper.createObjectNode();
        parsed.put("intent", "transactions");

        Time.normalizeTimeByText("查昨天的支出记录", parsed);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        assertEquals("day", parsed.path("timeRange").asText());
        assertEquals(String.valueOf(yesterday.getYear()), parsed.path("year").asText());
        assertEquals(String.valueOf(yesterday.getMonthValue()), parsed.path("month").asText());
        assertEquals(String.valueOf(yesterday.getDayOfMonth()), parsed.path("day").asText());
        assertTrue(!parsed.path("needMonth").asBoolean());
    }
}
