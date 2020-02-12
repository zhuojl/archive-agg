package com.zhuojl.share.compose.annotation;

import com.zhuojl.share.compose.config.ComposerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 允许自动组合
 * 参照EnableFeignClients
 * @author zhuojl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ComposerRegistrar.class)
public @interface EnableAutoCompose {

    /**
     * package names
     */
    String[] value();
}
