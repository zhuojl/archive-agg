package com.zhuojl.map.reduce.service;

import com.google.common.collect.Range;

import com.zhuojl.map.reduce.OrderArchiveKey;
import com.zhuojl.map.reduce.dto.OrderPageDTO;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 从mysql查询订单
 *
 * @author zhuojl
 */
@Service
@Slf4j
public class MysqlOrderServiceImpl2 extends MySqlOrderServiceImpl1 {

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public OrderArchiveKey getArchiveKey() {
        return new OrderArchiveKey(Range.closed(8, 9));
    }



}
