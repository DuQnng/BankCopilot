package com.idea.controller;

import com.idea.entity.Account;
import com.idea.entity.Result;
import com.idea.entity.TransactionRecord;
import com.idea.service.AccountService;
import com.idea.utils.CurrentHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
