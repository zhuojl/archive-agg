package com.zhuojl.map.reduce.service.order;

import com.google.common.collect.Range;

import com.zhuojl.map.reduce.MapReduceAble;
import com.zhuojl.map.reduce.SpElExpressionKeyResolver;
import com.zhuojl.map.reduce.SystemArchiveKey;
import com.zhuojl.map.reduce.annotation.MapReduce;
import com.zhuojl.map.reduce.annotation.SpelArchiveKeyExpression;
import com.zhuojl.map.reduce.annotation.MapReduceMethodConfig;
import com.zhuojl.map.reduce.archivekey.ArchiveKeyResolver;
import com.zhuojl.map.reduce.common.enums.ExecuteMode;
import com.zhuojl.map.reduce.dto.OrderPageDTO;
import com.zhuojl.map.reduce.dto.OrderQueryDTO;
import com.zhuojl.map.reduce.model.GroupBySth;
import com.zhuojl.map.reduce.model.Order;
import com.zhuojl.map.reduce.model.OrderStatistic;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 订单逻辑
 *
 * @author zhuojl
 */
@MapReduce
public interface OrderService extends MapReduceAble<SystemArchiveKey> {

    String SIMPLE_HANDLER = "orderService.CustomArchiveKeyResolver";
    String ORDER_QUERY_HANDLER = "orderService.ObjectParamHandler.OrderQueryDTO";


    /**
     * 根据创建人查询订单，从多个简单参数中获取参数
     */
    @MapReduceMethodConfig(paramHandler = SIMPLE_HANDLER)
    List<Order> listByMultiParam(String creator, Integer low, Integer high);


    /**
     * 根据创建人查询订单，从实体对象中获取参数
     *
     */
    @MapReduceMethodConfig(paramHandler = ORDER_QUERY_HANDLER)
    List<Order> listByDTO(OrderQueryDTO orderQueryDTO);


    /**
     * 根据创建人查询订单，从实体对象中获取参数
     */
    @MapReduceMethodConfig(paramHandler = SpElExpressionKeyResolver.SPEL_EXPRESSION_RESOLVER)
    @SpelArchiveKeyExpression(high = "#orderQueryDTO.high", low = "#orderQueryDTO.low")
    Integer getOrderCount(OrderQueryDTO orderQueryDTO);


    /**
     * 检测ForceReduceAble
     */
    @MapReduceMethodConfig(paramHandler = SpElExpressionKeyResolver.SPEL_EXPRESSION_RESOLVER)
    @SpelArchiveKeyExpression(high = "#orderQueryDTO.high", low = "#orderQueryDTO.low")
    OrderStatistic statistic(OrderQueryDTO orderQueryDTO);


    /**
     * 检测ReduceAble
     */
    @MapReduceMethodConfig(paramHandler = SpElExpressionKeyResolver.SPEL_EXPRESSION_RESOLVER)
    @SpelArchiveKeyExpression(high = "#orderQueryDTO.high", low = "#orderQueryDTO.low")
    List<GroupBySth> listGroupBy(OrderQueryDTO orderQueryDTO);


    /**
     * 测试 find first 模式
     */

    @MapReduceMethodConfig(executeMode = ExecuteMode.FIND_FIRST, paramHandler = SpElExpressionKeyResolver.SPEL_EXPRESSION_RESOLVER)
    @SpelArchiveKeyExpression(high = "#orderQueryDTO.high", low = "#orderQueryDTO.low")
    OrderStatistic findFirst(OrderQueryDTO orderQueryDTO);


    /**
     * 测试 分页 模式
     */
    @MapReduceMethodConfig(executeMode = ExecuteMode.PAGE, paramHandler = SpElExpressionKeyResolver.SPEL_EXPRESSION_RESOLVER)
    @SpelArchiveKeyExpression(high = "#orderQueryDTO.high", low = "#orderQueryDTO.low")
    OrderPageDTO page(OrderPageDTO orderQueryDTO);

    /**
     * {@link OrderService#page(com.zhuojl.map.reduce.dto.OrderPageDTO)} 伴生方法，不可删除
     *
     * FIXME 存在被认为是无用方法被删除的可能。。。。
     * @param orderPageDTO
     * @return
     */
    Integer pageCount(OrderPageDTO orderPageDTO);

    /**
     * 定制 简单参数处理类
     */
    @Component(SIMPLE_HANDLER)
    class CustomArchiveKeyResolver implements ArchiveKeyResolver<SystemArchiveKey> {
        @Override
        public SystemArchiveKey extract(Method method, Object... params) {

            Range<Integer> range = Range.closed(Integer.valueOf(String.valueOf(params[1])),
                    Integer.valueOf(String.valueOf(params[2])));

            return new SystemArchiveKey(range);

        }

    }

    /**
     * 定制 实体参数处理
     */
    @Component(ORDER_QUERY_HANDLER)
    class OrderQueryDTOCustomParamHandler implements ArchiveKeyResolver<SystemArchiveKey> {
        @Override
        public SystemArchiveKey extract(Method method, Object... params) {
            OrderQueryDTO orderQueryDTO = (OrderQueryDTO) params[0];
            return new SystemArchiveKey(Range.closed(orderQueryDTO.getLow(), orderQueryDTO.getHigh()));
        }

    }

}