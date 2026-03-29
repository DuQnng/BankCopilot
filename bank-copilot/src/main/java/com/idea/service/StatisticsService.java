package com.idea.service;

import com.idea.dto.StatisticsQueryDTO;
import com.idea.vo.StatisticsResultVO;

public interface StatisticsService {
    StatisticsResultVO statistics(StatisticsQueryDTO query);
}
