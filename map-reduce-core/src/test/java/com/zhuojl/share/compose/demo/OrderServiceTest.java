package com.zhuojl.share.compose.demo;

import com.zhuojl.share.compose.ShardingApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试
 *
 * @author zhuojl
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ShardingApplication.class)
@Slf4j
public class OrderServiceTest {

    @Test
    public void testMultiParam() {
    }


}