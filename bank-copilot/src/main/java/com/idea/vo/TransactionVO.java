package com.idea.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionVO {
    
    @ExcelProperty("交易时间")
    @ColumnWidth(25)
    private String tradeTime;
    
    @ExcelIgnore
    private String accountNo;
    
    @ExcelProperty("对方账号")
    @ColumnWidth(30)
    private String counterpartyAccountNo;
    
    @ExcelProperty("交易类型")
    private String type;
    
    @ExcelProperty("交易金额")
    private BigDecimal amount;
    
    @ExcelProperty("交易描述")
    @ColumnWidth(40)
    private String description;
}
