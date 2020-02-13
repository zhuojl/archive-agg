package com.zhuojl.map.reduce;


import com.zhuojl.map.reduce.annotation.EnableAutoMapReduce;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author zhuojl
 */
@SpringBootApplication
@EnableAutoMapReduce("com.zhuojl.map.reduce")
@EnableCaching
public class MapReduceApplication {


    public static void main(String[] args) {
        SpringApplication.run(MapReduceApplication.class, args);
    }

}
