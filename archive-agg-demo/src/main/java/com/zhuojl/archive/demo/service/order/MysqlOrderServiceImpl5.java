package com.zhuojl.archive.demo.service.order;

import com.google.common.collect.Range;

import com.zhuojl.archive.demo.SystemArchiveKey;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 从mysql查询订单
 *
 * @author zhuojl
 */
@Service
@Slf4j
public class MysqlOrderServiceImpl5 extends MySqlOrderServiceImpl1 {

    @Override
    public SystemArchiveKey getArchiveKey() {
        return new SystemArchiveKey(Range.closed(14, 15));
    }


}
