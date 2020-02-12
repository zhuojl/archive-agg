package com.zhuojl.share.compose.demo.service;

import com.google.common.collect.Range;

import com.zhuojl.share.compose.ComposeAble;
import com.zhuojl.share.compose.ComposeParam;
import com.zhuojl.share.compose.ComposeParamHandler;
import com.zhuojl.share.compose.annotation.ComposeMark;
import com.zhuojl.share.compose.annotation.MethodComposeConfig;
import com.zhuojl.share.compose.common.enums.MergeEnum;
import com.zhuojl.share.compose.demo.dto.OrderQueryDTO;
import com.zhuojl.share.compose.demo.model.Order;

import org.springframework.stereotype.Component;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 订单查询
 *
 * @author zhuojl
 */
@ComposeMark
public interface OrderService extends ComposeAble<OrderService.OrderServiceComposeParam> {

    String SIMPLE_HANDLER = "orderService.SimpleParamHandler.simple";
    String ORDER_QUERY_HANDLER = "orderService.ObjectParamHandler.OrderQueryDTO";

    /**
     * 每个实现都需要一个判断分段 getExecuteParam
     */
    OrderServiceComposeConfig getComposeConfig();


    /**
     * 根据创建人查询订单，从多个简单参数中获取参数
     */
    @MethodComposeConfig(paramHandler = SIMPLE_HANDLER, resultHandler = MergeEnum.SIMPLE_LIST)
    List<Order> listByMultiParam(String creator, Integer low, Integer high);


    /**
     * 根据创建人查询订单，从实体对象中获取参数
     */
    @MethodComposeConfig(paramHandler = ORDER_QUERY_HANDLER, resultHandler = MergeEnum.SIMPLE_LIST)
    List<Order> listByDTO(OrderQueryDTO orderQueryDTO);


     /**
     * 根据创建人查询订单，从实体对象中获取参数
     */
    @MethodComposeConfig(paramHandler = ORDER_QUERY_HANDLER, resultHandler = MergeEnum.SIMPLE_NUMBER)
    Integer getOrderCount(OrderQueryDTO orderQueryDTO);





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
