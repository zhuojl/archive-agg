package com.zhuojl.share.compose.demo;

import com.zhuojl.share.compose.ShardingApplication;
import com.zhuojl.share.compose.demo.dto.OrderQueryDTO;
import com.zhuojl.share.compose.demo.model.GroupBySth;
import com.zhuojl.share.compose.demo.model.OrderStatistic;
import com.zhuojl.share.compose.demo.service.OrderService;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

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
    @Autowired
    private OrderService orderService;

    @Test
    public void testMultiParam() {
        List list = orderService.listByMultiParam("zhuojl", 2, 5);
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testSingleParam() {
        OrderQueryDTO orderQueryDTO = getOrderQueryDTO();
        orderQueryDTO.setCreator("zhuojl");
        List list2 = orderService.listByDTO(orderQueryDTO);
        Assert.assertEquals(2, orderQueryDTO.getLow().intValue());
        Assert.assertEquals(5, orderQueryDTO.getHigh().intValue());
        Assert.assertNotNull(list2);
        Assert.assertNotNull(list2);
        Assert.assertEquals(2, list2.size());
    }


    @Test
    public void testGetCount() {
        OrderQueryDTO orderQueryDTO = getOrderQueryDTO();
        orderQueryDTO.setCreator("zhuojl");
        Integer count = orderService.getOrderCount(orderQueryDTO);
        Assert.assertEquals(2, orderQueryDTO.getLow().intValue());
        Assert.assertEquals(5, orderQueryDTO.getHigh().intValue());
        Assert.assertNotNull(count);
        Assert.assertEquals(2, count.intValue());
    }


    @Test
    public void testListGroupBy() {
        OrderQueryDTO orderQueryDTO = getOrderQueryDTO();
        orderQueryDTO.setCreator("zhuojl");
        List<GroupBySth> list = orderService.listGroupBy(orderQueryDTO);
        Assert.assertEquals(3, list.size());
    }


    @Test
    public void testGetGroupBy() {
        OrderQueryDTO orderQueryDTO = getOrderQueryDTO();
        orderQueryDTO.setCreator("zhuojl");
        OrderStatistic statistic = orderService.statistic(orderQueryDTO);
        Assert.assertEquals(4, statistic.getCount().intValue());
        Assert.assertEquals(3, statistic.getMoney().intValue());
    }


    private OrderQueryDTO getOrderQueryDTO() {
        OrderQueryDTO orderQueryDTO = new OrderQueryDTO();
        orderQueryDTO.setLow(2);
        orderQueryDTO.setHigh(5);
        return orderQueryDTO;
    }

}