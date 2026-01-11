package com.idea.controller;

import com.idea.mapper.UserMapper;
import com.idea.entity.Result;
import com.idea.entity.User;
import com.idea.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Slf4j
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    private Map<String, String> codeMap = new ConcurrentHashMap<>();

    @PostMapping("/code")
    public Result sendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        // 1. 验证手机号格式
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.error("手机号格式不正确");
        }

        // 2. 生成 6 位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));
        // 3. 打印到日志（开发阶段）
        codeMap.put(phone, code);
        log.info("手机号 {} 验证码为 {}", phone, code);
        // 4. 返回前端提示（可选返回 code）
        return Result.success("验证码已发送，请查看日志");
    }

    @PostMapping("/do")
    public Result doRegister(@RequestBody Map<String,String> body){
        String username = body.get("username");
        String password = body.get("password");
        String name = body.get("name");
        String phone = body.get("phone");
        String code = body.get("code");

        // 1. 校验必填
        if(username==null||password==null||name==null||phone==null||code==null){
            return Result.error("请填写完整信息");
        }

        // 2. 校验手机号格式
        if(!phone.matches("^1[3-9]\\d{9}$")){
            return Result.error("手机号格式不正确");
        }

        // 3. 校验验证码
        String cacheCode = codeMap.get(phone);
        if(cacheCode==null || !cacheCode.equals(code)){
            return Result.error("验证码错误或已过期");
        }

        // 4. 检查用户名是否存在
        User existUser = userMapper.selectByUsername(username);
        if(existUser != null){
            return Result.error("用户名已存在");
        }

        // 5. 存入数据库
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 可以考虑加密 passwordEncoder.encode(password)
        user.setName(name);
        user.setPhone(phone);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        int rows = userMapper.insert(user);
        if(rows>0){
            // 注册成功，删除验证码
            codeMap.remove(phone);
            return Result.success("注册成功，请登录");
        }else{
            return Result.error("注册失败");
        }
    }
}