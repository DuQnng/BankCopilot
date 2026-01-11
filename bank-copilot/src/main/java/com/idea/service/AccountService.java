package com.idea.service;

import com.idea.entity.Account;
import com.idea.entity.TransactionRecord;

import java.util.List;

public interface AccountService {

    Account getAccountInfo(Long userId);

    List<TransactionRecord> getRecentTransactions(Long userId);

}
