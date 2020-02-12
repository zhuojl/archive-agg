package com.zhuojl.share.proxy.annotation;

import java.lang.annotation.*;

/**
 * 标记需要代理的类
 *
 * @author zhuojl
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ShardingClient {
}
