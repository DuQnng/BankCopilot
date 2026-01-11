package com.idea.service.impl;

import com.idea.dto.TransferRequestDTO;
import com.idea.entity.Account;
import com.idea.entity.TransactionRecord;
import com.idea.mapper.AccountMapper;
import com.idea.mapper.TransactionMapper;
import com.idea.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Transactional
    public void transfer(Long fromUserId, TransferRequestDTO request) {
        // 1. 查询付款账户
        Account fromAccount = accountMapper.selectByUserId(fromUserId);
        if (fromAccount == null || fromAccount.getStatus().equals("冻结")) {
            throw new RuntimeException("账户不可用");
        }

        // 2. 查询收款账户
        Account toAccount = accountMapper.findByAccountNo(request.getToAccountNo());
        if (toAccount == null) {
            throw new RuntimeException("收款账户不存在");
        }

        // 3. 检查余额
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("余额不足");
        }

        BigDecimal amount = request.getAmount();

        // 4. 扣款
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountMapper.updateBalance(fromAccount);

        // 5. 收款
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountMapper.updateBalance(toAccount);

        // 6. 记录交易（付款）
        TransactionRecord outRecord = new TransactionRecord();
        outRecord.setTradeTime(LocalDateTime.now());//当前时间
        outRecord.setAccountId(fromAccount.getId());
        outRecord.setCounterpartyAccountNo(toAccount.getAccountNo());
        outRecord.setType("支出");
        outRecord.setAmount(amount.negate()); // 支出为负数
        outRecord.setDescription(request.getDescription());
        transactionMapper.insert(outRecord);

        // 7. 记录交易（收款）
        TransactionRecord inRecord = new TransactionRecord();
        inRecord.setTradeTime(LocalDateTime.now());
        inRecord.setAccountId(toAccount.getId());
        inRecord.setCounterpartyAccountNo(fromAccount.getAccountNo());
        inRecord.setType("收入");
        inRecord.setAmount(amount);
        inRecord.setDescription(request.getDescription());
        transactionMapper.insert(inRecord);
    }
}
