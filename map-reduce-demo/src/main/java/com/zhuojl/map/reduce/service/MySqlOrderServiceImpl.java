package com.zhuojl.map.reduce.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import com.zhuojl.map.reduce.OrderArchiveKey;
import com.zhuojl.map.reduce.dto.OrderQueryDTO;
import com.zhuojl.map.reduce.model.GroupBySth;
import com.zhuojl.map.reduce.model.Order;
import com.zhuojl.map.reduce.model.OrderStatistic;

import org.springframework.core.Ordered;
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
public class MySqlOrderServiceImpl implements OrderService, Ordered {

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public OrderArchiveKey getArchiveKey() {
        return new OrderArchiveKey(Range.closed(4, 7));
    }

    @Override
    public List<Order> listByMultiParam(String creator, Integer start, Integer end) {
        log.info("mysql creator : {},{}.{}", creator, start, end);
        List<Order> orders = new ArrayList<>();
        orders.add(Order.builder()
                .orderId(1L)
                .amount(100L)
                .creator("mysql : " + creator)
                .build());
        return orders;
    }

    @Override
    public List<Order> listByDTO(OrderQueryDTO orderQueryDTO) {
        log.info("mysql listByDTO : {}", orderQueryDTO);
        List<Order> orders = new ArrayList<>();
        orders.add(Order.builder()
                .orderId(1L)
                .amount(100L)
                .creator("mysql : " + orderQueryDTO.getCreator())
                .build());
        return orders;
    }

    @Override
    public Integer getOrderCount(OrderQueryDTO orderQueryDTO) {
        log.info("mysql count: {}", orderQueryDTO.getHigh() - orderQueryDTO.getLow());
        return orderQueryDTO.getHigh() - orderQueryDTO.getLow();
    }

    @Override
    public OrderStatistic statistic(OrderQueryDTO orderQueryDTO) {
        return new OrderStatistic("mysql demo", 2, 3);
    }

    @Override
    public List<GroupBySth> listGroupBy(OrderQueryDTO orderQueryDTO) {
        return Lists.newArrayList(new GroupBySth(2, 2), new GroupBySth(3, 2));
    }

}
