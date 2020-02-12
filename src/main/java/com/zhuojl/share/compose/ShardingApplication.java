package com.zhuojl.share.compose;


import com.zhuojl.share.compose.annotation.EnableAutoCompose;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author zhuojl
 */
@SpringBootApplication
@EnableAutoCompose("com.zhuojl.share.compose.demo")
@EnableCaching
public class ShardingApplication {


    public static void main(String[] args) {
        SpringApplication.run(ShardingApplication.class, args);
    }

}
