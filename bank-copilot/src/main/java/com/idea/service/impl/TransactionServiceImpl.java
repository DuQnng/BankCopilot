package com.idea.service.impl;

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
}
