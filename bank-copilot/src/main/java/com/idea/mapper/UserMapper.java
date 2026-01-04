package com.idea.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.idea.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {



    @Select("select id,username,name from user where username=#{username} and password=#{password}")
    User selectByUsernameAndPassword(User user);

    @Select("select * from user where id = #{id}")
    User selectById(Integer id);

    @Update("update user set password = #{password} where id = #{id}")
    int updateById(User user);

}
