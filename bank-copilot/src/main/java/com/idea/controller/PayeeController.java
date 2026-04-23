package com.idea.controller;

import com.idea.common.ErrorCode;
import com.idea.dto.PayeeCreateDTO;
import com.idea.dto.PayeeQueryDTO;
import com.idea.dto.PayeeTransferDTO;
import com.idea.dto.PayeeUpdateDTO;
import com.idea.entity.Result;
import com.idea.exception.BusinessException;
import com.idea.service.PayeeService;
import com.idea.utils.CurrentHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payees")
public class PayeeController {

    @Autowired
    private PayeeService payeeService;

    @GetMapping
    public Result list(PayeeQueryDTO query) {
        Integer userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return payeeService.list(userId.longValue(), query);
    }

    @PostMapping
    public Result create(@RequestBody PayeeCreateDTO dto) {
        Integer userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return payeeService.create(userId.longValue(), dto);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody PayeeUpdateDTO dto) {
        Integer userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return payeeService.update(userId.longValue(), id, dto);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        Integer userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return payeeService.delete(userId.longValue(), id);
    }

    @PostMapping("/{id}/transfer/validate")
    public Result validateTransfer(@PathVariable Long id, @RequestBody PayeeTransferDTO dto) {
        Integer userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return payeeService.validateTransfer(userId.longValue(), id, dto);
    }

    @PostMapping("/{id}/transfer")
    public Result transfer(@PathVariable Long id, @RequestBody PayeeTransferDTO dto) {
        Integer userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return payeeService.transfer(userId.longValue(), id, dto);
    }
}
