package com.zhuojl.share.proxy.service;

import com.zhuojl.share.proxy.annotation.ShardingClient;
import com.zhuojl.share.proxy.annotation.Sharding;
import com.zhuojl.share.proxy.common.enums.BinaryOperatorEnum;
import com.zhuojl.share.proxy.model.Order;

import java.util.Date;
import java.util.List;

/**
 * 订单查询
 *
 * @author zhuojl
 */
@ShardingClient
public interface OrderService extends ShardingAble {

    /**
     * 根据创建人查询订单
     */
    @Sharding(startTimeFiled = "start", endTimeField = "end", binaryOperator = BinaryOperatorEnum.LIST_MERGE)
    List<Order> list(String creator, Date start, Date end);

}
