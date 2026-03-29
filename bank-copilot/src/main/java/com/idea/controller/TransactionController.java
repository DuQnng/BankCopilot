package com.idea.controller;

import com.idea.dto.TransactionQueryDTO;
import com.idea.entity.Result;
import com.idea.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public Result list(TransactionQueryDTO query) {
        return transactionService.getTransactions(query);
    }
}
