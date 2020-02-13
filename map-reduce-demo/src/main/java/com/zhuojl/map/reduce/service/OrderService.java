package com.zhuojl.map.reduce.service;

import com.google.common.collect.Range;

import com.zhuojl.map.reduce.ArchiveKeyResolver;
import com.zhuojl.map.reduce.MapReduceAble;
import com.zhuojl.map.reduce.OrderArchiveKey;
import com.zhuojl.map.reduce.ParamWithArchiveKey;
import com.zhuojl.map.reduce.annotation.MapReduce;
import com.zhuojl.map.reduce.annotation.MapReduceMethodConfig;
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
     * 获取段 的执行参数 在这个实现中是通过与getComposeConfig配置做交集
     */
    @Override
    default OrderArchiveKey intersectionArchiveKey(OrderArchiveKey queryArchiveKey) {
        // 获取当前实现类的归档配置
        return getArchiveKey().intersection(queryArchiveKey);
    }


    /**
     * 定制 简单参数处理类
     */
    @Component(SIMPLE_HANDLER)
    class CustomArchiveKeyResolver implements ArchiveKeyResolver<OrderArchiveKey> {
        @Override
        public ParamWithArchiveKey<OrderArchiveKey> extract(Object... params) {

            Range<Integer> range = Range.closed(Integer.valueOf(String.valueOf(params[1])), Integer.valueOf(String.valueOf(params[2])));

            return ParamWithArchiveKey.<OrderArchiveKey>builder()
                    .originalParams(params)
                    .archiveKey(new OrderArchiveKey(range))
                    .build();

        }

        @Override
        public Object rebuild(ParamWithArchiveKey<OrderArchiveKey> paramWithArchiveKey) {
            paramWithArchiveKey.getOriginalParams()[1] = paramWithArchiveKey.getArchiveKey().getRange().lowerEndpoint();
            paramWithArchiveKey.getOriginalParams()[2] = paramWithArchiveKey.getArchiveKey().getRange().upperEndpoint();
            return paramWithArchiveKey.getOriginalParams();
        }
    }

    /**
     * 定制 实体参数处理
     */
    @Component(ORDER_QUERY_HANDLER)
    class OrderQueryDTOCustomParamHandler implements ArchiveKeyResolver<OrderArchiveKey> {
        @Override
        public ParamWithArchiveKey<OrderArchiveKey> extract(Object... params) {
            OrderQueryDTO orderQueryDTO = (OrderQueryDTO) params[0];
            return ParamWithArchiveKey.<OrderArchiveKey>builder()
                    .originalParams(params)
                    .archiveKey(new OrderArchiveKey(Range.closed(orderQueryDTO.getLow(), orderQueryDTO.getHigh())))
                    .build();
        }

        @Override
        public Object rebuild(ParamWithArchiveKey<OrderArchiveKey> paramWithArchiveKey) {
            OrderQueryDTO orderQueryDTO = (OrderQueryDTO) paramWithArchiveKey.getOriginalParams()[0];
            orderQueryDTO.setLow(paramWithArchiveKey.getArchiveKey().getRange().lowerEndpoint());
            orderQueryDTO.setHigh(paramWithArchiveKey.getArchiveKey().getRange().upperEndpoint());
            return paramWithArchiveKey.getOriginalParams();
        }
    }

}
