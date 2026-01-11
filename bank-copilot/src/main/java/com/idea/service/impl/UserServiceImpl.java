package com.idea.service.impl;

import com.idea.dto.ChangePasswordDTO;
import com.idea.mapper.UserMapper;
import com.idea.entity.*;
import com.idea.service.UserService;
import com.idea.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public LoginInfo login(User user) {
        User e = userMapper.selectByUsernameAndPassword(user);
        if(e!=null)
        {
            log.info("用户名：{}，密码：{}", user.getUsername(), user.getPassword());
            Map<String,Object> claims=new HashMap<>();
            claims.put("id",e.getId());
            claims.put("username",e.getUsername());
            claims.put("name",e.getName());
            String jwt = JwtUtils.generateJwt(claims);
            log.info("jwt = {}",jwt);
            return new LoginInfo(e.getId(),e.getUsername(),e.getName(),jwt);
        }
        return null;
    }

    @Override
    public Result changePassword(Integer userId, ChangePasswordDTO dto){

        log.info("Mapper class = {}", userMapper.getClass());
        User user = userMapper.selectById(userId);
        log.info("新密码 = {},旧密码={}",dto.getNewPassword(),dto.getOldPassword());
        if (!isPasswordMatch(dto.getOldPassword(), user.getPassword())) {
            return Result.error("原密码错误");
        }

        log.info("新密码 = {},旧密码={}",dto.getNewPassword(),user.getPassword());
        if (dto.getNewPassword().equals(user.getPassword())) {
            return Result.error("新密码不能与原密码相同");
        }

        if (!isPasswordStrong(dto.getNewPassword())) {
            return Result.error("新密码不符合安全规则");
        }

        user.setPassword(dto.getNewPassword());
        userMapper.updateById(user);
        return Result.success("密码修改成功");
    }

    private boolean isPasswordMatch(String newPassword, String password) {
        return newPassword != null && newPassword.equals(password);
    }

    private boolean isPasswordStrong(String password) {
        // 简单示例：至少8位，包含字母和数字
        return password.length() >= 8 && password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$");
    }

//    @Override
//    public PageResult<User> page(Integer page, Integer pageSize) {
//        Long total=empMapper.count();
//        List<User> rows=empMapper.list((page-1)*pageSize,pageSize);
//        return new PageResult<User>(total,rows);
//    }


}
