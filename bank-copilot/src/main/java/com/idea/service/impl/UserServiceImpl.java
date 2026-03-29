package com.idea.service.impl;

import com.idea.common.ErrorCode;
import com.idea.dto.ChangePasswordDTO;
import com.idea.exception.BusinessException;
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
            log.info("获取到的token = {}",jwt);
            return new LoginInfo(e.getId(),e.getUsername(),e.getName(),jwt);
        }
        return null;
    }

    @Override

    public Result changePassword(Integer userId, ChangePasswordDTO dto){

        User user = userMapper.selectById(userId);
        if (user == null) {
            // 这个属于“参数/用户状态异常”
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "用户不存在");
        }

        if (!isPasswordMatch(dto.getOldPassword(), user.getPassword())) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "原密码错误");
        }

        // ⚠️ 这里你原来写的是 newPassword.equals(user.getPassword())
        // 但 user.getPassword() 可能是旧密码（一般是加密后的），这判断意义不大
        // 如果你是明文存储，那OK；如果后面你要加密，建议直接用 oldPassword 判断
        if (dto.getNewPassword() != null && dto.getNewPassword().equals(user.getPassword())) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "新密码不能与原密码相同");
        }

        if (!isPasswordStrong(dto.getNewPassword())) {
            throw BusinessException.of(ErrorCode.PARAM_INVALID, "新密码不符合安全规则（至少8位，包含字母和数字）");
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
        if (password == null) return false;
        return password.length() >= 8 && password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$");
    }

//    @Override
//    public PageResult<User> page(Integer page, Integer pageSize) {
//        Long total=empMapper.count();
//        List<User> rows=empMapper.list((page-1)*pageSize,pageSize);
//        return new PageResult<User>(total,rows);
//    }


}
