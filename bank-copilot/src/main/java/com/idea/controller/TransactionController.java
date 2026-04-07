package com.idea.controller;

import com.idea.dto.TransactionQueryDTO;
import com.idea.entity.Result;
import com.idea.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public Result list(TransactionQueryDTO query) {
        return transactionService.getTransactions(query);
    }

    @GetMapping("/export")
    public void export(TransactionQueryDTO query, HttpServletResponse response) {
        transactionService.exportTransactions(query, response);
    }
}
