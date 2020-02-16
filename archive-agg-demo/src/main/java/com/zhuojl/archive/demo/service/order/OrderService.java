package com.zhuojl.archive.demo.service.order;

import com.google.common.collect.Range;

import com.zhuojl.archive.MapReduceAble;
import com.zhuojl.archive.demo.annotation.SpelArchiveKeyExpression;
import com.zhuojl.archive.common.enums.ExecuteMode;
import com.zhuojl.archive.demo.model.GroupBySth;
import com.zhuojl.archive.demo.model.Order;
import com.zhuojl.archive.demo.SpElExpressionKeyResolver;
import com.zhuojl.archive.demo.SystemArchiveKey;
import com.zhuojl.archive.demo.annotation.ArchiveAgg;
import com.zhuojl.archive.demo.annotation.ArchiveAggMethodConfig;
import com.zhuojl.archive.archivekey.ArchiveKeyResolver;
import com.zhuojl.archive.demo.dto.OrderPageDTO;
import com.zhuojl.archive.demo.dto.OrderQueryDTO;
import com.zhuojl.archive.demo.model.OrderStatistic;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 订单逻辑
 *
 * @author zhuojl
 */
@ArchiveAgg
public interface OrderService extends MapReduceAble<SystemArchiveKey> {

    String SIMPLE_HANDLER = "orderService.CustomArchiveKeyResolver";
    String ORDER_QUERY_HANDLER = "orderService.ObjectParamHandler.OrderQueryDTO";


    /**
     * 根据创建人查询订单，从多个简单参数中获取参数
     */
    @ArchiveAggMethodConfig(paramHandler = SIMPLE_HANDLER)
    List<Order> listByMultiParam(String creator, Integer low, Integer high);


    /**
     * 根据创建人查询订单，从实体对象中获取参数
     *
     */
    @ArchiveAggMethodConfig(paramHandler = ORDER_QUERY_HANDLER)
    List<Order> listByDTO(OrderQueryDTO orderQueryDTO);


    /**
     * 根据创建人查询订单，从实体对象中获取参数
     */
    @ArchiveAggMethodConfig(paramHandler = SpElExpressionKeyResolver.SPEL_EXPRESSION_RESOLVER)
    @SpelArchiveKeyExpression(high = "#orderQueryDTO.high", low = "#orderQueryDTO.low")
    Integer getOrderCount(OrderQueryDTO orderQueryDTO);


    /**
     * 检测ForceReduceAble
     */
    @ArchiveAggMethodConfig(paramHandler = SpElExpressionKeyResolver.SPEL_EXPRESSION_RESOLVER)
    @SpelArchiveKeyExpression(high = "#orderQueryDTO.high", low = "#orderQueryDTO.low")
    OrderStatistic statistic(OrderQueryDTO orderQueryDTO);


    /**
     * 检测ReduceAble
     */
    @ArchiveAggMethodConfig(paramHandler = SpElExpressionKeyResolver.SPEL_EXPRESSION_RESOLVER)
    @SpelArchiveKeyExpression(high = "#orderQueryDTO.high", low = "#orderQueryDTO.low")
    List<GroupBySth> listGroupBy(OrderQueryDTO orderQueryDTO);


    /**
     * 测试 find first 模式
     */

    @ArchiveAggMethodConfig(executeMode = ExecuteMode.FIND_FIRST, paramHandler = SpElExpressionKeyResolver.SPEL_EXPRESSION_RESOLVER)
    @SpelArchiveKeyExpression(high = "#orderQueryDTO.high", low = "#orderQueryDTO.low")
    OrderStatistic findFirst(OrderQueryDTO orderQueryDTO);


    /**
     * 测试 分页 模式
     */
    @ArchiveAggMethodConfig(executeMode = ExecuteMode.PAGE, paramHandler = SpElExpressionKeyResolver.SPEL_EXPRESSION_RESOLVER)
    @SpelArchiveKeyExpression(high = "#orderQueryDTO.high", low = "#orderQueryDTO.low")
    OrderPageDTO page(OrderPageDTO orderQueryDTO);

    /**
     * {@link OrderService#page(OrderPageDTO)} 伴生方法，不可删除
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
