package com.idea.service.impl;

import com.idea.common.ErrorCode;
import com.idea.dto.TransferRequestDTO;
import com.idea.entity.Account;
import com.idea.entity.TransactionRecord;
import com.idea.exception.BusinessException;
import com.idea.mapper.AccountMapper;
import com.idea.mapper.TransactionMapper;
import com.idea.service.AccountService;
import com.idea.vo.TransferValidateVO;
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

        // 0. 参数校验（给客服：参数不合法）
        if (request == null || request.getToAccountNo() == null || request.getToAccountNo().isBlank()
                || request.getAmount() == null) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "转账参数不完整");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "转账金额必须大于0");
        }

        // 1. 查询付款账户
        Account fromAccount = accountMapper.selectByUserId(fromUserId);
        if (fromAccount == null) {
            throw BusinessException.of(ErrorCode.ACCOUNT_NOT_FOUND, "付款账户不存在");
        }
        if ("冻结".equals(fromAccount.getStatus())) {
            throw BusinessException.of(ErrorCode.ACCOUNT_FROZEN, "付款账户已冻结");
        }

        // 2. 查询收款账户
        Account toAccount = accountMapper.findByAccountNo(request.getToAccountNo());
        if (toAccount == null) {
            throw BusinessException.of(ErrorCode.ACCOUNT_NOT_FOUND, "收款账户不存在");
        }
        if ("冻结".equals(toAccount.getStatus())) {
            throw BusinessException.of(ErrorCode.ACCOUNT_FROZEN, "收款账户状态异常");
        }

        // 2.1 不能给自己转账（你枚举里已经有）
        if (fromAccount.getAccountNo() != null && fromAccount.getAccountNo().equals(toAccount.getAccountNo())) {
            throw BusinessException.of(ErrorCode.TRANSFER_TO_SELF);
        }

        BigDecimal amount = request.getAmount();

        // 3. 检查余额
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw BusinessException.of(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // 4. 扣款
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountMapper.updateBalance(fromAccount);

        // 5. 收款
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountMapper.updateBalance(toAccount);

        // 6. 记录交易（付款）
        TransactionRecord outRecord = new TransactionRecord();
        outRecord.setTradeTime(LocalDateTime.now());
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

    @Override
    public TransferValidateVO validateTransfer(Long fromUserId, TransferRequestDTO request) {

        // 0. 参数校验
        if (request == null || request.getToAccountNo() == null || request.getToAccountNo().isBlank()
                || request.getAmount() == null) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "转账参数不完整");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "转账金额必须大于0");
        }

        // 1. 付款账户
        Account fromAccount = accountMapper.selectByUserId(fromUserId);
        if (fromAccount == null) {
            throw BusinessException.of(ErrorCode.ACCOUNT_NOT_FOUND, "付款账户不存在");
        }
        if ("冻结".equals(fromAccount.getStatus())) {
            throw BusinessException.of(ErrorCode.ACCOUNT_FROZEN, "付款账户已冻结");
        }

        // 2. 收款账户
        Account toAccount = accountMapper.findByAccountNo(request.getToAccountNo());
        if (toAccount == null) {
            throw BusinessException.of(ErrorCode.ACCOUNT_NOT_FOUND, "收款账户不存在");
        }
        if ("冻结".equals(toAccount.getStatus())) {
            throw BusinessException.of(ErrorCode.ACCOUNT_FROZEN, "收款账户状态异常");
        }

        // 2.1 不能给自己转账
        if (fromAccount.getAccountNo() != null && fromAccount.getAccountNo().equals(toAccount.getAccountNo())) {
            throw BusinessException.of(ErrorCode.TRANSFER_TO_SELF);
        }

        // 3. 余额校验
        BigDecimal amount = request.getAmount();
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw BusinessException.of(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // 4. 封装确认信息（脱敏）
        TransferValidateVO vo = new TransferValidateVO();
        vo.setFromAccountNoMasked(maskAccount(fromAccount.getAccountNo()));
        vo.setToAccountNoMasked(maskAccount(toAccount.getAccountNo()));
        vo.setAmount(amount);
        vo.setBalance(fromAccount.getBalance());
        vo.setDescription(request.getDescription());

        return vo;
    }

    // 简单脱敏：保留前4后4，中间****
    private String maskAccount(String accountNo) {
        if (accountNo == null || accountNo.length() < 8) return "****";
        return accountNo.substring(0, 4) + "****" + accountNo.substring(accountNo.length() - 4);
    }

}
