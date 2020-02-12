package com.zhuojl.share.compose.demo.service;

import com.google.common.collect.Range;

import com.zhuojl.share.compose.demo.dto.OrderQueryDTO;
import com.zhuojl.share.compose.demo.model.Order;

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
public class MySqlOrderServiceImpl implements OrderService {

    @Override
    public OrderServiceComposeConfig getComposeConfig() {
        OrderServiceComposeConfig composeConfig = new OrderServiceComposeConfig();
        composeConfig.setConfigRange(Range.closed(4, 7));
        return composeConfig;
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
}
