package com.idea.mapper;

import com.idea.entity.TransactionRecord;
import org.apache.ibatis.annotations.Insert;
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

    @Insert("INSERT INTO transaction_record " +
            "(account_id, counterparty_account_no, type, amount, description, trade_time) " +
            "VALUES " +
            "(#{accountId}, #{counterpartyAccountNo}, #{type}, #{amount}, #{description}, #{tradeTime})")
    void insert(TransactionRecord record);
}