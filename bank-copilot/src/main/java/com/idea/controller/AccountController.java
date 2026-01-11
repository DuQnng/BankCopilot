package com.idea.controller;

import com.idea.dto.TransferRequestDTO;
import com.idea.entity.Result;
import com.idea.service.AccountService;
import com.idea.utils.CurrentHolder;
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
        try {
            accountService.transfer(fromUserId.longValue(), request);
            return Result.success("转账成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
