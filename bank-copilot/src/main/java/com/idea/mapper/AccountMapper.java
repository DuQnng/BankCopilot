package com.idea.mapper;

import com.idea.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AccountMapper {

    @Select("select * from account where user_id = #{userId} limit 1")
    Account selectByUserId(Long userId);

    @Select("select name from user where id = #{userId} ")
    String findAccountNameByUserId(Long userId);
}
