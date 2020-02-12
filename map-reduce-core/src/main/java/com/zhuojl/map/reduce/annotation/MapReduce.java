package com.zhuojl.map.reduce.annotation;

import java.lang.annotation.*;

/**
 * 标记需要组合的类
 *
 * @author zhuojl
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MapReduce {
}
