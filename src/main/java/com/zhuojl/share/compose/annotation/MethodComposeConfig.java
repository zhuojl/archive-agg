package com.zhuojl.share.compose.annotation;


import com.zhuojl.share.compose.common.enums.MergeEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 可以标记方法如何处理、结果集合如何处理、是否并发执行、是否快速返回。
 *
 * @author zhuojl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface MethodComposeConfig {

    /**
     * 方法参数处理器 beanName
     * @return
     */
    String paramHandler();

    /**
     *
     * @return
     */
    MergeEnum resultHandler();

}
