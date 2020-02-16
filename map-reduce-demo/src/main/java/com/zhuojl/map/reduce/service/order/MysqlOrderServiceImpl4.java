package com.zhuojl.map.reduce.service.order;

import com.google.common.collect.Range;

import com.zhuojl.map.reduce.SystemArchiveKey;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 从mysql查询订单
 *
 * @author zhuojl
 */
@Service
@Slf4j
public class MysqlOrderServiceImpl4 extends MySqlOrderServiceImpl1 {

    @Override
    public SystemArchiveKey getArchiveKey() {
        return new SystemArchiveKey(Range.closed(12, 13));
    }


}
