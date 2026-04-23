package com.idea.service;

import com.idea.dto.PayeeCreateDTO;
import com.idea.dto.PayeeQueryDTO;
import com.idea.dto.PayeeTransferDTO;
import com.idea.dto.PayeeUpdateDTO;
import com.idea.entity.Result;

public interface PayeeService {
    Result list(Long userId, PayeeQueryDTO query);

    Result create(Long userId, PayeeCreateDTO dto);

    Result update(Long userId, Long id, PayeeUpdateDTO dto);

    Result delete(Long userId, Long id);

    Result validateTransfer(Long userId, Long payeeId, PayeeTransferDTO dto);

    Result transfer(Long userId, Long payeeId, PayeeTransferDTO dto);
}
