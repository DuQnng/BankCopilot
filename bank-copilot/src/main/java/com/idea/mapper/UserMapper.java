package com.idea.mapper;


import com.idea.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {


    @Select("select id,username,name from user where username=#{username} and password=#{password}")
    User selectByUsernameAndPassword(User user);

    @Select("select * from user where id = #{id}")
    User selectById(Integer id);

    @Update("update user set password = #{password} where id = #{id}")
    int updateById(User user);

    @Select("select * from user where username=#{username}")
    User selectByUsername(String username);

    @Insert("insert into user(username,password,name,phone,create_time,update_time) values(#{username},#{password},#{name},#{phone},#{createTime},#{updateTime})")
    int insert(User user);
}
