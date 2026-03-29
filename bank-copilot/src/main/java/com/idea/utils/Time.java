package com.idea.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idea.dto.StatisticsQueryDTO;
import com.idea.dto.TransactionQueryDTO;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Time {

    private Time() {
    }

    public static void fillTimeRange(TransactionQueryDTO q,
                                     String timeRange,
                                     String yearStr,
                                     String monthStr,
                                     String dayStr,
                                     String rangeValueStr) {

        LocalDate today = LocalDate.now();

        Integer year = parseInteger(yearStr);
        Integer month = parseInteger(monthStr);
        Integer day = parseInteger(dayStr);
        Integer rangeValue = parseInteger(rangeValueStr);

        switch (timeRange) {
            case "month": {
                int y = (year != null) ? year : today.getYear();
                int m = (month != null) ? month : today.getMonthValue();
                LocalDate firstDay = LocalDate.of(y, m, 1);
                LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
                q.setStartTime(String.valueOf(firstDay.atStartOfDay()));
                q.setEndTime(String.valueOf(lastDay.atTime(23, 59, 59)));
                break;
            }
            case "day": {
                int y = (year != null) ? year : today.getYear();
                int m = (month != null) ? month : today.getMonthValue();
                int d = (day != null) ? day : today.getDayOfMonth();
                LocalDate date = LocalDate.of(y, m, d);
                q.setStartTime(String.valueOf(date.atStartOfDay()));
                q.setEndTime(String.valueOf(date.atTime(23, 59, 59)));
                break;
            }
            case "recent_days": {
                int days = (rangeValue != null && rangeValue > 0) ? rangeValue : 7;
                LocalDate start = today.minusDays(days - 1L);
                q.setStartTime(String.valueOf(start.atStartOfDay()));
                q.setEndTime(String.valueOf(today.atTime(23, 59, 59)));
                break;
            }
            case "recent_years": {
                int years = (rangeValue != null && rangeValue > 0) ? rangeValue : 3;
                LocalDate start = today.minusYears(years);
                q.setStartTime(String.valueOf(start.atStartOfDay()));
                q.setEndTime(String.valueOf(today.atTime(23, 59, 59)));
                break;
            }
            case "latest":
            default:
                break;
        }
    }

    //方法重载：区别仅在于没有分页
    public static void fillTimeRange(StatisticsQueryDTO q,
                                     String timeRange,
                                     String yearStr,
                                     String monthStr,
                                     String dayStr,
                                     String rangeValueStr) {

        LocalDate today = LocalDate.now();

        Integer year = parseInteger(yearStr);
        Integer month = parseInteger(monthStr);
        Integer day = parseInteger(dayStr);
        Integer rangeValue = parseInteger(rangeValueStr);

        switch (timeRange) {
            case "month": {
                int y = (year != null) ? year : today.getYear();
                int m = (month != null) ? month : today.getMonthValue();
                LocalDate firstDay = LocalDate.of(y, m, 1);
                LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
                q.setStartTime(String.valueOf(firstDay.atStartOfDay()));
                q.setEndTime(String.valueOf(lastDay.atTime(23, 59, 59)));
                break;
            }
            case "day": {
                int y = (year != null) ? year : today.getYear();
                int m = (month != null) ? month : today.getMonthValue();
                int d = (day != null) ? day : today.getDayOfMonth();
                LocalDate date = LocalDate.of(y, m, d);
                q.setStartTime(String.valueOf(date.atStartOfDay()));
                q.setEndTime(String.valueOf(date.atTime(23, 59, 59)));
                break;
            }
            case "recent_days": {
                int days = (rangeValue != null && rangeValue > 0) ? rangeValue : 7;
                LocalDate start = today.minusDays(days - 1L);
                q.setStartTime(String.valueOf(start.atStartOfDay()));
                q.setEndTime(String.valueOf(today.atTime(23, 59, 59)));
                q.setRangeValue(days);
                break;
            }
            case "recent_years": {
                int years = (rangeValue != null && rangeValue > 0) ? rangeValue : 3;
                LocalDate start = today.minusYears(years);
                q.setStartTime(String.valueOf(start.atStartOfDay()));
                q.setEndTime(String.valueOf(today.atTime(23, 59, 59)));
                q.setRangeValue(years);
                break;
            }
            case "latest":
            default:
                break;
        }
    }



    public static Integer parseInteger(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public static int extractMonth(JsonNode parsed, String msg) {
        Integer m = parseInteger(parsed.path("month").asText(""));
        if (m != null) {
            return m;
        }

        Matcher matcher = Pattern.compile("(\\d{1,2})月").matcher(msg);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    /**
     * 对“今天/昨天/今年/本月/最近几天/最近几年”做后端纠偏
     */
    public static JsonNode normalizeTimeByText(String msg, JsonNode parsed) {
        if (!(parsed instanceof ObjectNode node)) {
            return parsed;
        }

        LocalDate today = LocalDate.now();

        if (msg.contains("今天")) {
            node.put("timeRange", "day");
            node.put("year", String.valueOf(today.getYear()));
            node.put("month", String.valueOf(today.getMonthValue()));
            node.put("day", String.valueOf(today.getDayOfMonth()));
            node.put("needMonth", false);
            return node;
        }

        if (msg.contains("昨天")) {
            LocalDate d = today.minusDays(1);
            node.put("timeRange", "day");
            node.put("year", String.valueOf(d.getYear()));
            node.put("month", String.valueOf(d.getMonthValue()));
            node.put("day", String.valueOf(d.getDayOfMonth()));
            node.put("needMonth", false);
            return node;
        }

        if (msg.contains("本月") || msg.contains("这个月")) {
            node.put("timeRange", "month");
            node.put("year", String.valueOf(today.getYear()));
            node.put("month", String.valueOf(today.getMonthValue()));
            node.put("needMonth", false);
            return node;
        }

        if (msg.contains("今年")) {
            node.put("year", String.valueOf(today.getYear()));
        }

        if (msg.contains("前几天")) {
            node.put("timeRange", "recent_days");
            node.put("rangeValue", "3");
            node.put("needMonth", false);
            return node;
        }

        if (msg.contains("最近几天")||msg.contains("这几天")) {
            node.put("timeRange", "recent_days");
            node.put("rangeValue", "7");
            node.put("needMonth", false);
            return node;
        }

        if (msg.contains("最近几年") || msg.contains("前几年")) {
            node.put("timeRange", "recent_years");
            node.put("rangeValue", "3");
            node.put("needMonth", false);
            return node;
        }

        return node;
    }
}
