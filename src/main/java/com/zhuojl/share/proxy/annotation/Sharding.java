package com.zhuojl.share.proxy.annotation;


import com.zhuojl.share.proxy.common.enums.BinaryOperatorEnum;

import java.lang.annotation.*;


/**
 * @author zhuojl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Sharding {

    /**
     * 开始时间字段
     *
     * @return
     */
    String startTimeFiled();

    /**
     * 结束时间字段
     *
     * @return
     */
    String endTimeField();

    /**
     * 合并策略
     * @return
     */
    BinaryOperatorEnum binaryOperator();


}
