package com.idea.service.impl;

import com.alibaba.excel.EasyExcel;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.idea.dto.TransactionQueryDTO;
import com.idea.entity.PageResult;
import com.idea.entity.Result;
import com.idea.mapper.TransactionMapper;
import com.idea.service.TransactionService;
import com.idea.vo.TransactionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    public Result getTransactions(TransactionQueryDTO query) {

        PageHelper.startPage(query.getPage(), query.getSize());

        List<TransactionVO> list = transactionMapper.selectTransactions(query);

        PageInfo<TransactionVO> pageInfo = new PageInfo<>(list);

        PageResult<TransactionVO> pageResult = new PageResult<>(
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getList()
        );

        return Result.success(pageResult);
    }

    @Override
    public void exportTransactions(TransactionQueryDTO query, HttpServletResponse response) {
        try {
            // 查询所有符合条件的数据，不进行分页
            List<TransactionVO> list = transactionMapper.selectTransactions(query);
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码
            String fileName = URLEncoder.encode("交易流水", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 写入Excel
            EasyExcel.write(response.getOutputStream(), TransactionVO.class)
                    .sheet("流水记录")
                    .doWrite(list);
                    
        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }
}
