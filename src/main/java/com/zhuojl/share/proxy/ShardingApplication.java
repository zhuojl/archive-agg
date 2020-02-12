package com.zhuojl.share.proxy;


import com.zhuojl.share.proxy.annotation.EnableAutoSharding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author zhuojl
 */
@SpringBootApplication
@EnableAutoSharding("com.zhuojl.share.proxy.service")
@EnableCaching
public class ShardingApplication {


    public static void main(String[] args) {
        SpringApplication.run(ShardingApplication.class, args);
    }

}
