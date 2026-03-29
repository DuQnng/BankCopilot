package com.idea.service;

import com.idea.dto.TransferRequestDTO;
import com.idea.entity.Account;
import com.idea.entity.TransactionRecord;
import com.idea.vo.TransferValidateVO;

import java.util.List;

public interface AccountService {

    Account getAccountInfo(Long userId);

    List<TransactionRecord> getRecentTransactions(Long userId);

    void transfer(Long fromUserId, TransferRequestDTO request);

    TransferValidateVO validateTransfer(Long fromUserId, TransferRequestDTO request);

}
