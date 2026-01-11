package com.idea.service.impl;

import com.idea.entity.Account;
import com.idea.entity.TransactionRecord;
import com.idea.mapper.AccountMapper;
import com.idea.mapper.TransactionMapper;
import com.idea.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    public Account getAccountInfo(Long userId) {
        Account account = accountMapper.selectByUserId(userId);
        account.setAccountName(accountMapper.findAccountNameByUserId(userId));
        return account;
    }

    @Override
    public List<TransactionRecord> getRecentTransactions(Long userId) {
        Account account = accountMapper.selectByUserId(userId);
        if (account == null) {
            return Collections.emptyList();
        }
        return transactionMapper.selectRecentByAccountId(account.getId());
    }
}
