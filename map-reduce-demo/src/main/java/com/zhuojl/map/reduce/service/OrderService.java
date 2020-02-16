package com.zhuojl.map.reduce.service;

import com.google.common.collect.Range;

import com.zhuojl.map.reduce.MapReduceAble;
import com.zhuojl.map.reduce.OrderArchiveKey;
import com.zhuojl.map.reduce.annotation.MapReduce;
import com.zhuojl.map.reduce.annotation.MapReduceMethodConfig;
import com.zhuojl.map.reduce.archivekey.ArchiveKeyResolver;
import com.zhuojl.map.reduce.common.enums.ExecuteMode;
import com.zhuojl.map.reduce.dto.OrderPageDTO;
import com.zhuojl.map.reduce.dto.OrderQueryDTO;
import com.zhuojl.map.reduce.model.GroupBySth;
import com.zhuojl.map.reduce.model.Order;
import com.zhuojl.map.reduce.model.OrderStatistic;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单逻辑
 *
 * @author zhuojl
 */
@MapReduce
public interface OrderService extends MapReduceAble<OrderArchiveKey> {

    String SIMPLE_HANDLER = "orderService.CustomArchiveKeyResolver";
    String ORDER_QUERY_HANDLER = "orderService.ObjectParamHandler.OrderQueryDTO";
    String ORDER_PAGE_HANDLER = "orderService.ObjectParamHandler.OrderPageDTO";


    /**
     * 根据创建人查询订单，从多个简单参数中获取参数
     */
    @MapReduceMethodConfig(paramHandler = SIMPLE_HANDLER)
    List<Order> listByMultiParam(String creator, Integer low, Integer high);


    /**
     * 根据创建人查询订单，从实体对象中获取参数
     */
    @MapReduceMethodConfig(paramHandler = ORDER_QUERY_HANDLER)
    List<Order> listByDTO(OrderQueryDTO orderQueryDTO);


    /**
     * 根据创建人查询订单，从实体对象中获取参数
     */
    @MapReduceMethodConfig(paramHandler = ORDER_QUERY_HANDLER)
    Integer getOrderCount(OrderQueryDTO orderQueryDTO);


    /**
     * 检测ForceReduceAble
     */
    @MapReduceMethodConfig(paramHandler = ORDER_QUERY_HANDLER)
    OrderStatistic statistic(OrderQueryDTO orderQueryDTO);


    /**
     * 检测ReduceAble
     */
    @MapReduceMethodConfig(paramHandler = ORDER_QUERY_HANDLER)
    List<GroupBySth> listGroupBy(OrderQueryDTO orderQueryDTO);


    /**
     * 测试 find first 模式
     */
    @MapReduceMethodConfig(executeMode = ExecuteMode.FIND_FIRST, paramHandler = ORDER_QUERY_HANDLER)
    OrderStatistic findFirst(OrderQueryDTO orderQueryDTO);


    /**
     * 测试 分页 模式
     */
    @MapReduceMethodConfig(executeMode = ExecuteMode.PAGE, paramHandler = ORDER_PAGE_HANDLER)
    OrderPageDTO page(OrderPageDTO orderQueryDTO);


    /**
     * 定制 简单参数处理类
     */
    @Component(SIMPLE_HANDLER)
    class CustomArchiveKeyResolver implements ArchiveKeyResolver<OrderArchiveKey> {
        @Override
        public OrderArchiveKey extract(Object... params) {

            Range<Integer> range = Range.closed(Integer.valueOf(String.valueOf(params[1])),
                    Integer.valueOf(String.valueOf(params[2])));

            return new OrderArchiveKey(range);

        }

        @Override
        public Object[] rebuild(OrderArchiveKey archiveKey, Object... params) {
            params[1] = archiveKey.getRange().lowerEndpoint();
            params[2] = archiveKey.getRange().upperEndpoint();
            return params;
        }
    }

    /**
     * 定制 实体参数处理
     */
    @Component(ORDER_QUERY_HANDLER)
    class OrderQueryDTOCustomParamHandler implements ArchiveKeyResolver<OrderArchiveKey> {
        @Override
        public OrderArchiveKey extract(Object... params) {
            OrderQueryDTO orderQueryDTO = (OrderQueryDTO) params[0];
            return new OrderArchiveKey(Range.closed(orderQueryDTO.getLow(), orderQueryDTO.getHigh()));
        }

        @Override
        public Object[] rebuild(OrderArchiveKey archiveKey, Object... params) {
            OrderQueryDTO orderQueryDTO = (OrderQueryDTO) params[0];
            orderQueryDTO.setLow(archiveKey.getRange().lowerEndpoint());
            orderQueryDTO.setHigh(archiveKey.getRange().upperEndpoint());
            return params;
        }
    }

    /**
     * 定制 实体参数处理
     */
    @Component(ORDER_PAGE_HANDLER)
    class OrderPageDTOCustomParamHandler implements ArchiveKeyResolver<OrderArchiveKey> {
        @Override
        public OrderArchiveKey extract(Object... params) {
            OrderPageDTO orderQueryDTO = (OrderPageDTO) params[0];
            return new OrderArchiveKey(Range.closed(orderQueryDTO.getLow(), orderQueryDTO.getHigh()));
        }

        @Override
        public Object[] rebuild(OrderArchiveKey archiveKey, Object... params) {
            OrderPageDTO orderQueryDTO = (OrderPageDTO) params[0];
            orderQueryDTO.setLow(archiveKey.getRange().lowerEndpoint());
            orderQueryDTO.setHigh(archiveKey.getRange().upperEndpoint());
            return params;
        }
    }

}
