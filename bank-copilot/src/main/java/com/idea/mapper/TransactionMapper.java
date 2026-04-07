package com.idea.mapper;

import com.idea.dto.StatisticsQueryDTO;
import com.idea.dto.TransactionQueryDTO;
import com.idea.entity.TransactionRecord;
import com.idea.vo.StatisticsPointVO;
import com.idea.vo.TransactionVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
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

    // === 新增：分页查询
    List<TransactionVO> selectTransactions(@Param("query") TransactionQueryDTO query);

    // === 新增：计数
    long countTransactions(@Param("query") TransactionQueryDTO query);

    /*
    下边为统计数据专用Mapper
     */

    // === 总金额统计
    BigDecimal sumAmount(@Param("query") StatisticsQueryDTO query);

    // === 笔数统计
    Long countByStatistics(@Param("query") StatisticsQueryDTO query);

    // === 最大一笔
    StatisticsPointVO maxTransaction(@Param("query") StatisticsQueryDTO query);

    // === 趋势统计
    List<StatisticsPointVO> statisticsTrend(@Param("query") StatisticsQueryDTO query);

    // === 收入支出两柱子汇总统计
    List<StatisticsPointVO> sumIncomeAndExpense(@Param("query") StatisticsQueryDTO query);

    // === 按对方账号分组统计 (饼图)
    List<StatisticsPointVO> statisticsByCounterparty(@Param("query") StatisticsQueryDTO query);

}