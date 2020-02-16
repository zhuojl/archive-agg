package com.zhuojl.archive.demo.annotation;

import java.lang.annotation.*;

/**
 * 标记需要组合的类
 * 类似FeignClient
 *
 * @author zhuojl
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ArchiveAgg {
}
