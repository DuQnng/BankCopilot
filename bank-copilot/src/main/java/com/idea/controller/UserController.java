package com.idea.controller;

import com.idea.dto.ChangePasswordDTO;
import com.idea.pojo.Result;
import com.idea.service.UserService;
import com.idea.utils.CurrentHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @PostMapping("/change_password")
    public Result changePassword(@RequestBody ChangePasswordDTO dto) {

        Integer userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        log.info("修改密码，userId={}, dto={}", userId, dto);
        log.info("修改密码，当前用户ID：{}", userId);
        return userService.changePassword(userId, dto);

    }
}
