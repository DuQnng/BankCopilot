package com.idea.service;

import com.idea.dto.TransactionQueryDTO;
import com.idea.entity.Result;

import jakarta.servlet.http.HttpServletResponse;

public interface TransactionService {
    Result getTransactions(TransactionQueryDTO query);

    /**
     * 导出交易流水到Excel
     */
    void exportTransactions(TransactionQueryDTO query, HttpServletResponse response);
}