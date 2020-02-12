package com.zhuojl.share.compose.demo.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import com.zhuojl.share.compose.demo.dto.OrderQueryDTO;
import com.zhuojl.share.compose.demo.model.GroupBySth;
import com.zhuojl.share.compose.demo.model.Order;
import com.zhuojl.share.compose.demo.model.OrderStatistic;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 从mongo查询订单
 *
 * @author zhuojl
 */
@Service
@Slf4j
public class MongoOrderServiceImpl implements OrderService {



    @Override
    public OrderServiceComposeConfig getComposeConfig() {
        OrderServiceComposeConfig composeConfig = new OrderServiceComposeConfig();
        composeConfig.setConfigRange(Range.closed(1, 3));
        return composeConfig;
    }

    @Override
    public List<Order> listByMultiParam(String creator, Integer start, Integer end) {
        log.info("mongo creator : {},{}.{}", creator, start, end);
        List<Order> orders = new ArrayList<>();
        orders.add(Order.builder()
                .orderId(1L)
                .amount(100L)
                .creator("mongo : " + creator)
                .build());
        return orders;
    }

    @Override
    public List<Order> listByDTO(OrderQueryDTO orderQueryDTO) {
        log.info("mongo listByDTO : {}", orderQueryDTO);
        List<Order> orders = new ArrayList<>();
        orders.add(Order.builder()
                .orderId(1L)
                .amount(100L)
                .creator("mongo : " + orderQueryDTO.getCreator())
                .build());
        return orders;
    }


    @Override
    public Integer getOrderCount(OrderQueryDTO orderQueryDTO) {
        log.info("mongo count: {}", orderQueryDTO.getHigh() - orderQueryDTO.getLow());
        return orderQueryDTO.getHigh() - orderQueryDTO.getLow();
    }

    @Override
    public OrderStatistic statistic(OrderQueryDTO orderQueryDTO) {
        return new OrderStatistic("mongo demo", 1, 1);
    }

    @Override
    public List<GroupBySth> listGroupBy(OrderQueryDTO orderQueryDTO) {
        return Lists.newArrayList(new GroupBySth(2, 1), new GroupBySth(4, 2));
    }
}
