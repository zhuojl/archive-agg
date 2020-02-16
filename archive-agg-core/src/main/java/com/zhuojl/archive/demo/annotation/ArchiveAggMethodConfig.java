package com.zhuojl.archive.demo.annotation;



import com.zhuojl.archive.common.enums.ExecuteMode;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 配置方法元数据
 *
 * @author zhuojl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ArchiveAggMethodConfig {

    /**
     * 执行模式
     * @return
     */
    ExecuteMode executeMode() default ExecuteMode.ALL;

    /**
     *
     * 方法参数处理器 beanName，用于提取 归档键
     * @return
     */
    String paramHandler();

    /**
     * 数据合并beanName
     * @return
     */
    String aggregate() default "";


}
