package com.zhuojl.archive.annotation;

import com.zhuojl.archive.config.ArchiveAggRegistrar;

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
@Import(ArchiveAggRegistrar.class)
public @interface EnableAutoAgg {

    /**
     * package names
     */
    String[] value();
}
