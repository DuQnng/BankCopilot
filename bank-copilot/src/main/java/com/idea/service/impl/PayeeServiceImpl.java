package com.idea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.idea.common.ErrorCode;
import com.idea.dto.PayeeCreateDTO;
import com.idea.dto.PayeeQueryDTO;
import com.idea.dto.PayeeTransferDTO;
import com.idea.dto.PayeeUpdateDTO;
import com.idea.dto.TransferRequestDTO;
import com.idea.entity.PageResult;
import com.idea.entity.PayeeContact;
import com.idea.entity.Result;
import com.idea.exception.BusinessException;
import com.idea.mapper.AccountMapper;
import com.idea.mapper.PayeeContactMapper;
import com.idea.service.AccountService;
import com.idea.service.PayeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PayeeServiceImpl implements PayeeService {

    @Autowired
    private PayeeContactMapper payeeContactMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AccountService accountService;

    @Override
    public Result list(Long userId, PayeeQueryDTO query) {
        PayeeQueryDTO q = query == null ? new PayeeQueryDTO() : query;
        normalizePage(q);
        q.setPayeeName(normalizeText(q.getPayeeName()));
        q.setAccountNo(normalizeText(q.getAccountNo()));
        q.setAlias(normalizeText(q.getAlias()));

        PageHelper.startPage(q.getPage(), q.getSize());
        List<PayeeContact> list = payeeContactMapper.selectPage(userId, q);
        PageInfo<PayeeContact> pageInfo = new PageInfo<>(list);
        PageResult<PayeeContact> pageResult = new PageResult<>(
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getList()
        );
        return Result.success(pageResult);
    }

    @Override
    public Result create(Long userId, PayeeCreateDTO dto) {
        if (dto == null) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "请求体不能为空");
        }
        String accountNo = normalizeText(dto.getAccountNo());
        String alias = normalizeNullableAlias(dto.getAlias());

        validateCreateFields(accountNo);

        PayeeContact existing = payeeContactMapper.selectByUserIdAndAccountNo(userId, accountNo);
        if (existing != null) {
            throw BusinessException.of(ErrorCode.PAYEE_ALREADY_EXISTS);
        }

        String payeeName = normalizeText(accountMapper.findUserNameByAccountNo(accountNo));
        if (payeeName == null) {
            throw BusinessException.of(ErrorCode.ACCOUNT_NOT_FOUND, "该银行卡号不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        PayeeContact entity = new PayeeContact();
        entity.setUserId(userId);
        entity.setPayeeName(payeeName);
        entity.setAccountNo(accountNo);
        entity.setAlias(alias);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);

        payeeContactMapper.insert(entity);
        return Result.success(entity);
    }

    @Override
    public Result update(Long userId, Long id, PayeeUpdateDTO dto) {
        if (id == null || id <= 0) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "id 不合法");
        }
        if (dto == null) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "请求体不能为空");
        }

        PayeeContact existing = payeeContactMapper.selectByIdAndUserId(id, userId);
        if (existing == null) {
            throw BusinessException.of(ErrorCode.PAYEE_NOT_FOUND);
        }

        boolean hasAlias = dto.getAlias() != null;
        if (!hasAlias) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "请传入 alias 字段");
        }

        String alias = normalizeNullableAlias(dto.getAlias());

        PayeeContact toUpdate = new PayeeContact();
        toUpdate.setId(id);
        toUpdate.setUserId(userId);
        toUpdate.setAlias(alias);
        toUpdate.setUpdateTime(LocalDateTime.now());

        int rows = payeeContactMapper.updateByIdAndUserId(toUpdate);
        if (rows == 0) {
            throw BusinessException.of(ErrorCode.PAYEE_NOT_FOUND);
        }

        PayeeContact updated = payeeContactMapper.selectByIdAndUserId(id, userId);
        return Result.success(updated);
    }

    @Override
    public Result delete(Long userId, Long id) {
        if (id == null || id <= 0) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "id 不合法");
        }
        int rows = payeeContactMapper.deleteByIdAndUserId(id, userId);
        if (rows == 0) {
            throw BusinessException.of(ErrorCode.PAYEE_NOT_FOUND);
        }
        return Result.success("删除成功");
    }

    @Override
    public Result validateTransfer(Long userId, Long payeeId, PayeeTransferDTO dto) {
        PayeeContact payee = getPayeeById(userId, payeeId);
        TransferRequestDTO request = buildTransferRequest(payee, dto);
        return Result.success(accountService.validateTransfer(userId, request));
    }

    @Override
    public Result transfer(Long userId, Long payeeId, PayeeTransferDTO dto) {
        PayeeContact payee = getPayeeById(userId, payeeId);
        TransferRequestDTO request = buildTransferRequest(payee, dto);
        accountService.transfer(userId, request);
        return Result.success("转账成功");
    }

    private void normalizePage(PayeeQueryDTO q) {
        if (q.getPage() == null || q.getPage() < 1) {
            q.setPage(1);
        }
        if (q.getSize() == null || q.getSize() < 1 || q.getSize() > 100) {
            q.setSize(10);
        }
    }

    private void validateCreateFields(String accountNo) {
        if (accountNo == null) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "银行卡号不能为空");
        }
        if (!accountNo.matches("^\\d{8,32}$")) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "银行卡号格式不正确");
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }

    private String normalizeNullableAlias(String alias) {
        if (alias == null) {
            return null;
        }
        String text = alias.trim();
        return text.isEmpty() ? null : text;
    }

    private PayeeContact getPayeeById(Long userId, Long payeeId) {
        if (payeeId == null || payeeId <= 0) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "payeeId 不合法");
        }
        PayeeContact payee = payeeContactMapper.selectByIdAndUserId(payeeId, userId);
        if (payee == null) {
            throw BusinessException.of(ErrorCode.PAYEE_NOT_FOUND);
        }
        return payee;
    }

    private TransferRequestDTO buildTransferRequest(PayeeContact payee, PayeeTransferDTO dto) {
        if (dto == null || dto.getAmount() == null) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "转账金额不能为空");
        }
        if (dto.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "转账金额必须大于0");
        }

        TransferRequestDTO request = new TransferRequestDTO();
        request.setToAccountNo(payee.getAccountNo());
        request.setAmount(dto.getAmount());
        String description = normalizeNullableAlias(dto.getDescription());
        if (description == null) {
            description = payee.getAlias();
        }
        request.setDescription(description);
        return request;
    }
}
