package com.idea.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FaqQueryLog {
    private Long id;
    private Long userId;
    private String userQuery;
    private Long matchedFaqId;
    private BigDecimal matchScore;
    private String resultType;
    private Integer userFeedback;
    private LocalDateTime createTime;
}
