package com.zhuojl.share.proxy.service;

import com.zhuojl.share.proxy.model.Order;
import com.zhuojl.share.proxy.common.basic.TimeRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 从mysql查询订单
 *
 * @author zhuojl
 */
@Service
@Slf4j
public class MySqlOrderServiceImpl implements OrderService {

    @Override
    public TimeRange getTimeRange(Date startTime, Date endTime) {
        return new TimeRange(startTime, endTime);
    }

    @Override
    public List<Order> list(String creator, Date start, Date end) {
        log.info("mysql creator : {}", creator);
        List<Order> orders = new ArrayList<>();
        orders.add(Order.builder()
                .orderId(1L)
                .amount(100L)
                .creator("mysql : " + creator)
                .build());
        return orders;
    }
}
