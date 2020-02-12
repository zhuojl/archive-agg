package com.zhuojl.share.proxy.annotation;

import com.zhuojl.share.proxy.config.ShardingClientsRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 参照EnableFeignClients
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ShardingClientsRegistrar.class)
public @interface EnableAutoSharding {

    String[] value();
}
