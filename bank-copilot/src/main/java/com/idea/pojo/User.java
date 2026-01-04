package com.idea.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id; //ID,主键
    private String username; //用户名
    private String password; //密码
    private String phone; //手机号
    private String name;//名字
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //修改时间

}