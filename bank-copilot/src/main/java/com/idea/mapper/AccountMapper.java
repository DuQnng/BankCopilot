package com.idea.mapper;

import com.idea.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AccountMapper {

    @Select("select * from account where user_id = #{userId} limit 1")
    Account selectByUserId(Long userId);

    @Select("select name from user where id = #{userId} ")
    String findAccountNameByUserId(Long userId);

    @Select("select * from account where account_no = #{toAccountNo} limit 1 ")
    Account findByAccountNo(String toAccountNo);

    @Update("UPDATE account SET balance = #{balance} WHERE account_no = #{accountNo}")
    void updateBalance(Account account);
}
