package com.idea.controller;

import com.idea.common.ErrorCode;
import com.idea.dto.TransferRequestDTO;
import com.idea.entity.Result;
import com.idea.exception.BusinessException;
import com.idea.service.AccountService;
import com.idea.utils.CurrentHolder;
import com.idea.vo.TransferValidateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/info")
    public Result info() {
        Integer userId = CurrentHolder.getCurrentId();
        return Result.success(accountService.getAccountInfo(userId.longValue()));
    }

    @GetMapping("/transactions")
    public Result transactions() {
        Integer userId = CurrentHolder.getCurrentId();
        return Result.success(accountService.getRecentTransactions(userId.longValue()));
    }

    @PostMapping("/transfer")
    public Result transfer(@RequestBody TransferRequestDTO request) {
        Integer fromUserId = CurrentHolder.getCurrentId();
        // ✅ 没登录就抛 UNAUTHORIZED（别返回 Result.error）
        if (fromUserId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }

        accountService.transfer(fromUserId.longValue(), request);
        return Result.success("转账成功");
    }

    @PostMapping("/transfer/validate")
    public Result validate(@RequestBody TransferRequestDTO request) {
        Integer fromUserId = CurrentHolder.getCurrentId();
        if (fromUserId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        TransferValidateVO vo = accountService.validateTransfer(fromUserId.longValue(), request);
        return Result.success(vo);
    }

}
