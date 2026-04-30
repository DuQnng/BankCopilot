package com.idea.service.impl;

import com.idea.common.ErrorCode;
import com.idea.dto.TransferRequestDTO;
import com.idea.entity.Account;
import com.idea.exception.BusinessException;
import com.idea.mapper.AccountMapper;
import com.idea.mapper.TransactionMapper;
import com.idea.vo.TransferValidateVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void validateTransferShouldReturnMaskedAccountsWhenValid() {
        when(accountMapper.selectByUserId(1L)).thenReturn(account(1L, 1L, "6222000012345678", "正常", "1000.00"));
        when(accountMapper.findByAccountNo("6222000098765432")).thenReturn(account(2L, 2L, "6222000098765432", "正常", "300.00"));

        TransferValidateVO result = accountService.validateTransfer(1L, transfer("6222000098765432", "200.00"));

        assertEquals("6222****5678", result.getFromAccountNoMasked());
        assertEquals("6222****5432", result.getToAccountNoMasked());
        assertEquals(new BigDecimal("200.00"), result.getAmount());
    }

    @Test
    void validateTransferShouldRejectInsufficientBalance() {
        when(accountMapper.selectByUserId(1L)).thenReturn(account(1L, 1L, "6222000012345678", "正常", "100.00"));
        when(accountMapper.findByAccountNo("6222000098765432")).thenReturn(account(2L, 2L, "6222000098765432", "正常", "300.00"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> accountService.validateTransfer(1L, transfer("6222000098765432", "200.00")));

        assertEquals(ErrorCode.INSUFFICIENT_BALANCE.code(), ex.getBizCode());
    }

    @Test
    void validateTransferShouldRejectTransferToSelf() {
        when(accountMapper.selectByUserId(1L)).thenReturn(account(1L, 1L, "6222000012345678", "正常", "1000.00"));
        when(accountMapper.findByAccountNo("6222000012345678")).thenReturn(account(1L, 1L, "6222000012345678", "正常", "1000.00"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> accountService.validateTransfer(1L, transfer("6222000012345678", "10.00")));

        assertEquals(ErrorCode.TRANSFER_TO_SELF.code(), ex.getBizCode());
        assertTrue(ex.getMessage().contains("不能给自己转账"));
    }

    private Account account(Long id, Long userId, String accountNo, String status, String balance) {
        Account account = new Account();
        account.setId(id);
        account.setUserId(userId);
        account.setAccountNo(accountNo);
        account.setStatus(status);
        account.setBalance(new BigDecimal(balance));
        return account;
    }

    private TransferRequestDTO transfer(String toAccountNo, String amount) {
        TransferRequestDTO dto = new TransferRequestDTO();
        dto.setToAccountNo(toAccountNo);
        dto.setAmount(new BigDecimal(amount));
        dto.setDescription("测试转账");
        return dto;
    }
}
