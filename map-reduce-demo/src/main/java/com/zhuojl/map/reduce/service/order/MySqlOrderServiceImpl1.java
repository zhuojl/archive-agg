package com.zhuojl.map.reduce.service.order;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import com.zhuojl.map.reduce.SystemArchiveKey;
import com.zhuojl.map.reduce.dto.OrderPageDTO;
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
public class MySqlOrderServiceImpl1 implements OrderService {

    @Override
    public SystemArchiveKey getArchiveKey() {
        return new SystemArchiveKey(Range.closed(4, 7));
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
        return new OrderStatistic("mysql", 2, 3);
    }

    @Override
    public List<GroupBySth> listGroupBy(OrderQueryDTO orderQueryDTO) {
        return Lists.newArrayList(new GroupBySth(2, 2), new GroupBySth(3, 2));
    }

    @Override
    public OrderStatistic findFirst(OrderQueryDTO orderQueryDTO) {
        log.info("return null although order is higher");
        return null;
    }


    /**
     * 模拟测试， 实际查询时是根据start和limit查询的，在
     *
     * {@link com.zhuojl.map.reduce.config.MapReducePageAdjuster#adjustParam(java.lang.Integer, java.lang.Object[])}
     *
     * 中，处理了start和limit
     */
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
        log.info("order:{}, list:{}", getArchiveKey().getOrder(), list);
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
