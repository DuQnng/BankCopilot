package com.idea.mapper;

import com.idea.entity.TransactionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TransactionMapper {

    @Select("""
        select * from transaction_record
        where account_id = #{accountId}
        order by trade_time desc
        limit 10
    """)
    List<TransactionRecord> selectRecentByAccountId(Long accountId);

}