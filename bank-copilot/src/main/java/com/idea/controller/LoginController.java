package com.idea.controller;

import com.idea.common.ErrorCode;
import com.idea.entity.User;
import com.idea.entity.LoginInfo;
import com.idea.entity.Result;
import com.idea.exception.BusinessException;
import com.idea.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserService userService;
    @PostMapping
    public Result login(@RequestBody User user) {
        log.info("用户名：{}，密码：{}", user.getUsername(), user.getPassword());
        LoginInfo info = userService.login(user);
        if(info!=null) {
            return Result.success(info);
        }
        throw BusinessException.of(ErrorCode.LOGIN_FAILED);
    }



}