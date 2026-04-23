package com.idea.mapper;

import com.idea.dto.PayeeQueryDTO;
import com.idea.entity.PayeeContact;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PayeeContactMapper {

    List<PayeeContact> selectPage(@Param("userId") Long userId, @Param("query") PayeeQueryDTO query);

    @Select("select * from payee_contact where id = #{id} and user_id = #{userId} limit 1")
    PayeeContact selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("select * from payee_contact where user_id = #{userId} and account_no = #{accountNo} limit 1")
    PayeeContact selectByUserIdAndAccountNo(@Param("userId") Long userId, @Param("accountNo") String accountNo);

    @Select("select * from payee_contact where user_id = #{userId} and alias = #{alias} order by update_time desc, id desc")
    List<PayeeContact> selectByUserIdAndAlias(@Param("userId") Long userId, @Param("alias") String alias);

    @Insert("insert into payee_contact(user_id, payee_name, account_no, alias, create_time, update_time) " +
            "values(#{userId}, #{payeeName}, #{accountNo}, #{alias}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PayeeContact payeeContact);

    @Update("update payee_contact set alias = #{alias}, update_time = #{updateTime} " +
            "where id = #{id} and user_id = #{userId}")
    int updateByIdAndUserId(PayeeContact payeeContact);

    @Delete("delete from payee_contact where id = #{id} and user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
