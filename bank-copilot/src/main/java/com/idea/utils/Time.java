package com.idea.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idea.dto.StatisticsQueryDTO;
import com.idea.dto.TransactionQueryDTO;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;
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
            case "year": {
                int y = (year != null) ? year : today.getYear();
                LocalDate firstDay = LocalDate.of(y, 1, 1);
                LocalDate lastDay = LocalDate.of(y, 12, 31);
                q.setStartTime(String.valueOf(firstDay.atStartOfDay()));
                q.setEndTime(String.valueOf(lastDay.atTime(23, 59, 59)));
                break;
            }
            case "week": {
                WeekFields wf = WeekFields.of(Locale.CHINA);
                LocalDate start = today.with(wf.dayOfWeek(), 1);
                LocalDate end = start.plusDays(6);
                q.setStartTime(String.valueOf(start.atStartOfDay()));
                q.setEndTime(String.valueOf(end.atTime(23, 59, 59)));
                break;
            }
            case "last_week": {
                WeekFields wf = WeekFields.of(Locale.CHINA);
                LocalDate start = today.with(wf.dayOfWeek(), 1).minusWeeks(1);
                LocalDate end = start.plusDays(6);
                q.setStartTime(String.valueOf(start.atStartOfDay()));
                q.setEndTime(String.valueOf(end.atTime(23, 59, 59)));
                break;
            }
            case "quarter": {
                int m = today.getMonthValue();
                int cq = (m - 1) / 3;
                int startMonth = cq * 3 + 1;
                LocalDate first = LocalDate.of(today.getYear(), startMonth, 1);
                LocalDate last = first.plusMonths(3).minusDays(1);
                q.setStartTime(String.valueOf(first.atStartOfDay()));
                q.setEndTime(String.valueOf(last.atTime(23, 59, 59)));
                break;
            }
            case "last_quarter": {
                int m = today.getMonthValue();
                int cq = (m - 1) / 3;
                int y;
                int startMonth;
                if (cq == 0) {
                    y = today.getYear() - 1;
                    startMonth = 10;
                } else {
                    y = today.getYear();
                    startMonth = (cq - 1) * 3 + 1;
                }
                LocalDate first = LocalDate.of(y, startMonth, 1);
                LocalDate last = first.plusMonths(3).minusDays(1);
                q.setStartTime(String.valueOf(first.atStartOfDay()));
                q.setEndTime(String.valueOf(last.atTime(23, 59, 59)));
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
            case "year": {
                int y = (year != null) ? year : today.getYear();
                LocalDate firstDay = LocalDate.of(y, 1, 1);
                LocalDate lastDay = LocalDate.of(y, 12, 31);
                q.setStartTime(String.valueOf(firstDay.atStartOfDay()));
                q.setEndTime(String.valueOf(lastDay.atTime(23, 59, 59)));
                break;
            }
            case "week": {
                WeekFields wf = WeekFields.of(Locale.CHINA);
                LocalDate start = today.with(wf.dayOfWeek(), 1);
                LocalDate end = start.plusDays(6);
                q.setStartTime(String.valueOf(start.atStartOfDay()));
                q.setEndTime(String.valueOf(end.atTime(23, 59, 59)));
                break;
            }
            case "last_week": {
                WeekFields wf = WeekFields.of(Locale.CHINA);
                LocalDate start = today.with(wf.dayOfWeek(), 1).minusWeeks(1);
                LocalDate end = start.plusDays(6);
                q.setStartTime(String.valueOf(start.atStartOfDay()));
                q.setEndTime(String.valueOf(end.atTime(23, 59, 59)));
                break;
            }
            case "quarter": {
                int m = today.getMonthValue();
                int cq = (m - 1) / 3;
                int startMonth = cq * 3 + 1;
                LocalDate first = LocalDate.of(today.getYear(), startMonth, 1);
                LocalDate last = first.plusMonths(3).minusDays(1);
                q.setStartTime(String.valueOf(first.atStartOfDay()));
                q.setEndTime(String.valueOf(last.atTime(23, 59, 59)));
                break;
            }
            case "last_quarter": {
                int m = today.getMonthValue();
                int cq = (m - 1) / 3;
                int y;
                int startMonth;
                if (cq == 0) {
                    y = today.getYear() - 1;
                    startMonth = 10;
                } else {
                    y = today.getYear();
                    startMonth = (cq - 1) * 3 + 1;
                }
                LocalDate first = LocalDate.of(y, startMonth, 1);
                LocalDate last = first.plusMonths(3).minusDays(1);
                q.setStartTime(String.valueOf(first.atStartOfDay()));
                q.setEndTime(String.valueOf(last.atTime(23, 59, 59)));
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
     * 对自然语言时间词做后端纠偏（与 {@link #fillTimeRange} 中 timeRange 取值一致）。
     */
    public static JsonNode normalizeTimeByText(String msg, JsonNode parsed) {
        if (!(parsed instanceof ObjectNode node)) {
            return parsed;
        }

        LocalDate today = LocalDate.now();

        // 日：注意“大前天”含“前天”、“大后天”含“后天”，须先匹配长的
        if (msg.contains("大前天")) {
            LocalDate d = today.minusDays(3);
            putDay(node, d);
            return node;
        }
        if (msg.contains("前天")) {
            putDay(node, today.minusDays(2));
            return node;
        }
        if (msg.contains("今天") || msg.contains("今日")) {
            putDay(node, today);
            return node;
        }
        if (msg.contains("昨天") || msg.contains("昨日")) {
            putDay(node, today.minusDays(1));
            return node;
        }
        if (msg.contains("大后天")) {
            putDay(node, today.plusDays(3));
            return node;
        }
        if (msg.contains("后天")) {
            putDay(node, today.plusDays(2));
            return node;
        }
        if (msg.contains("明天") || msg.contains("明日")) {
            putDay(node, today.plusDays(1));
            return node;
        }

        // 月：“上上个月”含“上个月”
        if (msg.contains("上上个月") || msg.contains("上上月") || msg.contains("前两月")) {
            LocalDate first = today.minusMonths(2).withDayOfMonth(1);
            putCalendarMonth(node, first);
            return node;
        }
        if (msg.contains("上个月") || msg.contains("上月")) {
            LocalDate first = today.minusMonths(1).withDayOfMonth(1);
            putCalendarMonth(node, first);
            return node;
        }
        if (msg.contains("本月") || msg.contains("这个月")) {
            putCalendarMonth(node, today.withDayOfMonth(1));
            return node;
        }
        if (msg.contains("下个月") || msg.contains("下月")) {
            LocalDate first = today.plusMonths(1).withDayOfMonth(1);
            putCalendarMonth(node, first);
            return node;
        }

        if (msg.contains("本季度") || msg.contains("这个季度") || msg.contains("本季")) {
            node.put("timeRange", "quarter");
            node.put("needMonth", false);
            return node;
        }
        if (msg.contains("上季度") || msg.contains("上个季度") || msg.contains("上一季")) {
            node.put("timeRange", "last_quarter");
            node.put("needMonth", false);
            return node;
        }

        if (msg.contains("本周") || msg.contains("这周") || msg.contains("这个星期") || msg.contains("这一周")) {
            node.put("timeRange", "week");
            node.put("needMonth", false);
            return node;
        }
        if (msg.contains("上周") || msg.contains("上星期") || msg.contains("上个星期")) {
            node.put("timeRange", "last_week");
            node.put("needMonth", false);
            return node;
        }

        // 年：“大前年”含“前年”
        if (msg.contains("大前年")) {
            node.put("timeRange", "year");
            node.put("year", String.valueOf(today.getYear() - 3));
            node.put("needMonth", false);
            return node;
        }
        if (msg.contains("前年")) {
            node.put("timeRange", "year");
            node.put("year", String.valueOf(today.getYear() - 2));
            node.put("needMonth", false);
            return node;
        }
        if (msg.contains("去年")) {
            node.put("timeRange", "year");
            node.put("year", String.valueOf(today.getYear() - 1));
            node.put("needMonth", false);
            return node;
        }
        if (msg.contains("明年")) {
            node.put("timeRange", "year");
            node.put("year", String.valueOf(today.getYear() + 1));
            node.put("needMonth", false);
            return node;
        }

        if (msg.contains("今年")) {
            node.put("year", String.valueOf(today.getYear()));
        }

        if (msg.contains("近两年") || msg.contains("前两年") || msg.contains("最近两年") || msg.contains("最近二年")) {
            node.put("timeRange", "recent_years");
            node.put("rangeValue", "2");
            node.put("needMonth", false);
            return node;
        }

        if (msg.contains("前几天")) {
            node.put("timeRange", "recent_days");
            node.put("rangeValue", "3");
            node.put("needMonth", false);
            return node;
        }

        if (msg.contains("最近几天") || msg.contains("这几天")) {
            node.put("timeRange", "recent_days");
            node.put("rangeValue", "7");
            node.put("needMonth", false);
            return node;
        }

        if (msg.contains("近一周") || msg.contains("最近一周")) {
            node.put("timeRange", "recent_days");
            node.put("rangeValue", "7");
            node.put("needMonth", false);
            return node;
        }
        if (msg.contains("近一个月") || msg.contains("最近一个月") || msg.contains("近一月") || msg.contains("最近一月")) {
            node.put("timeRange", "recent_days");
            node.put("rangeValue", "30");
            node.put("needMonth", false);
            return node;
        }
        if (msg.contains("近两个月") || msg.contains("最近两个月") || msg.contains("近两月") || msg.contains("最近两月")) {
            node.put("timeRange", "recent_days");
            node.put("rangeValue", "60");
            node.put("needMonth", false);
            return node;
        }
        if (msg.contains("近三个月") || msg.contains("最近三个月") || msg.contains("近三月") || msg.contains("最近三月")) {
            node.put("timeRange", "recent_days");
            node.put("rangeValue", "90");
            node.put("needMonth", false);
            return node;
        }
        if (msg.contains("近半年") || msg.contains("最近半年")) {
            node.put("timeRange", "recent_days");
            node.put("rangeValue", "183");
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

    private static void putDay(ObjectNode node, LocalDate d) {
        node.put("timeRange", "day");
        node.put("year", String.valueOf(d.getYear()));
        node.put("month", String.valueOf(d.getMonthValue()));
        node.put("day", String.valueOf(d.getDayOfMonth()));
        node.put("needMonth", false);
    }

    private static void putCalendarMonth(ObjectNode node, LocalDate anyDayInMonth) {
        node.put("timeRange", "month");
        node.put("year", String.valueOf(anyDayInMonth.getYear()));
        node.put("month", String.valueOf(anyDayInMonth.getMonthValue()));
        node.put("needMonth", false);
    }
}
