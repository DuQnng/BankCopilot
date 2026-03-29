package com.idea.service;

import com.idea.dto.TransactionQueryDTO;
import com.idea.entity.Result;

public interface TransactionService {
    Result getTransactions(TransactionQueryDTO query);
}