package com.zhuojl.archive.demo.service.order;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import com.zhuojl.archive.demo.model.GroupBySth;
import com.zhuojl.archive.demo.model.Order;
import com.zhuojl.archive.demo.SystemArchiveKey;
import com.zhuojl.archive.demo.dto.OrderPageDTO;
import com.zhuojl.archive.demo.dto.OrderQueryDTO;
import com.zhuojl.archive.demo.model.OrderStatistic;

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

    public static final String NAME = "mongo";

    @Override
    public SystemArchiveKey getArchiveKey() {
        return new SystemArchiveKey(Range.closed(1, 3));
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
        return new OrderStatistic(NAME, 1, 1);
    }

    @Override
    public List<GroupBySth> listGroupBy(OrderQueryDTO orderQueryDTO) {
        return Lists.newArrayList(new GroupBySth(2, 1), new GroupBySth(4, 2));
    }

    @Override
    public OrderStatistic findFirst(OrderQueryDTO orderQueryDTO) {
        return new OrderStatistic(NAME, 1, 1);
    }

    @Override
    public OrderPageDTO page(OrderPageDTO orderQueryDTO) {
        OrderPageDTO orderPageDTO = new OrderPageDTO();

        Range<Integer> intersection = getArchiveKey().getRange()
                .intersection(Range.closed(orderQueryDTO.getLow(), orderQueryDTO.getHigh()));

        List<String> list = new ArrayList<>();
        int start = orderQueryDTO.getStart();
        for (int i = intersection.upperEndpoint() - start; i >= intersection.lowerEndpoint(); i--) {
            list.add("index:" + i);
        }
        log.info("order:{}, list:{}", NAME, list);
        orderPageDTO.setData(list);
        return orderPageDTO;
    }

    /**
     * 模拟测试，数量为查询与getArchiveKey的交集
     */
    @Override
    public Integer pageCount(OrderPageDTO orderPageDTO) {
        Range<Integer> intersection = getArchiveKey().getRange()
                .intersection(Range.closed(orderPageDTO.getLow(), orderPageDTO.getHigh()));
        return intersection.upperEndpoint() - intersection.lowerEndpoint() + 1;
    }
}
