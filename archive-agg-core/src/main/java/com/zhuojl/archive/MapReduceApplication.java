package com.zhuojl.archive;


import com.zhuojl.archive.demo.annotation.EnableAutoAgg;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author zhuojl
 */
@SpringBootApplication
@EnableAutoAgg("com.zhuojl.archive")
@EnableCaching
public class MapReduceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MapReduceApplication.class, args);
    }

}
