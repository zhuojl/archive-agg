package com.zhuojl.map.reduce.annotation;

import com.zhuojl.map.reduce.config.MapReduceRegistrar;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 允许自动组合 归档实现，是开起这套框架的入口
 * 参照EnableFeignClients
 * @author zhuojl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(MapReduceRegistrar.class)
public @interface EnableAutoMapReduce {

    /**
     * package names
     */
    String[] value();
}
