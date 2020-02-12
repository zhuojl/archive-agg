package com.zhuojl.map.reduce.service;

import com.google.common.collect.Range;

import com.zhuojl.map.reduce.MapReduceAble;
import com.zhuojl.map.reduce.ComposeParam;
import com.zhuojl.map.reduce.ComposeParamHandler;
import com.zhuojl.map.reduce.annotation.MapReduce;
import com.zhuojl.map.reduce.annotation.MapReduceMethodConfig;
import com.zhuojl.map.reduce.dto.OrderQueryDTO;
import com.zhuojl.map.reduce.model.GroupBySth;
import com.zhuojl.map.reduce.model.Order;
import com.zhuojl.map.reduce.model.OrderStatistic;

import org.springframework.stereotype.Component;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 订单逻辑
 *
 * @author zhuojl
 */
@MapReduce
public interface OrderService extends MapReduceAble<OrderService.OrderServiceComposeParam> {

    String SIMPLE_HANDLER = "orderService.SimpleParamHandler.simple";
    String ORDER_QUERY_HANDLER = "orderService.ObjectParamHandler.OrderQueryDTO";

    /**
     * 每个实现都需要一个判断分段 getExecuteParam,
     *
     * 这里有个问题，同样归档规则的业务有很多，不可能每一个都在实现类里配置配置一下，
     */
    OrderServiceComposeConfig getComposeConfig();


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
     * @param originalParam
     * @return
     */
    @Override
    default OrderServiceComposeParam getExecuteParam(OrderServiceComposeParam originalParam) {
        OrderServiceComposeConfig config = getComposeConfig();
        if (!originalParam.getRange().isConnected(config.getConfigRange())) {
            return null;
        }
        originalParam.setRange(originalParam.getRange().intersection(config.getConfigRange()));
        return originalParam;
    }

    /**
     * 区间配置
     */
    @Setter
    @Getter
    @ToString
    class OrderServiceComposeConfig {
        Range<Integer> configRange;
    }

    /**
     * 订单分段参数
     */
    @Setter
    @Getter
    @ToString
    class OrderServiceComposeParam implements ComposeParam {
        /**
         * 原始参数
         */
        Object[] originalParams;
        /**
         * orderService 的 区间参数
         */
        Range<Integer> range;
    }

    /**
     * 简单参数处理类
     */
    @Component(SIMPLE_HANDLER)
    class SimpleParamHandler implements ComposeParamHandler<OrderServiceComposeParam> {
        @Override
        public OrderServiceComposeParam extract(Object... params) {
            OrderServiceComposeParam composeParam = new OrderServiceComposeParam();
            composeParam.setOriginalParams(params);
            composeParam.setRange(Range.closed(Integer.valueOf(String.valueOf(params[1])), Integer.valueOf(String.valueOf(params[2]))));
            return composeParam;
        }

        @Override
        public Object rebuild(OrderServiceComposeParam composeParam) {
            composeParam.getOriginalParams()[1] = composeParam.getRange().lowerEndpoint();
            composeParam.getOriginalParams()[2] = composeParam.getRange().upperEndpoint();
            return composeParam.getOriginalParams();
        }
    }

    /**
     * 实体参数处理
     */
    @Component(ORDER_QUERY_HANDLER)
    class OrderQueryParamHandler implements ComposeParamHandler<OrderServiceComposeParam> {
        @Override
        public OrderServiceComposeParam extract(Object... params) {
            OrderServiceComposeParam composeParam = new OrderServiceComposeParam();
            composeParam.setOriginalParams(params);
            OrderQueryDTO orderQueryDTO = (OrderQueryDTO) params[0];
            composeParam.setRange(Range.closed(orderQueryDTO.getLow(), orderQueryDTO.getHigh()));
            return composeParam;
        }

        @Override
        public Object rebuild(OrderServiceComposeParam composeParam) {
            OrderQueryDTO orderQueryDTO = (OrderQueryDTO) composeParam.getOriginalParams()[0];
            orderQueryDTO.setLow(composeParam.getRange().lowerEndpoint());
            orderQueryDTO.setHigh(composeParam.getRange().upperEndpoint());
            return composeParam.getOriginalParams();
        }
    }

}