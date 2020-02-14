package com.zhuojl.map.reduce;

import com.zhuojl.map.reduce.dto.OrderQueryDTO;
import com.zhuojl.map.reduce.model.GroupBySth;
import com.zhuojl.map.reduce.model.OrderStatistic;
import com.zhuojl.map.reduce.service.OrderService;

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
@SpringBootTest(classes = MapReduceApplication.class)
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

        Integer count = orderService.getOrderCount(orderQueryDTO);
        Assert.assertEquals(2, orderQueryDTO.getLow().intValue());
        Assert.assertEquals(5, orderQueryDTO.getHigh().intValue());
        Assert.assertNotNull(count);
        Assert.assertEquals(2, count.intValue());
    }


    @Test
    public void testListGroupBy() {
        OrderQueryDTO orderQueryDTO = getOrderQueryDTO();
        List<GroupBySth> list = orderService.listGroupBy(orderQueryDTO);
        Assert.assertEquals(3, list.size());
    }


    @Test
    public void testGetGroupBy() {
        OrderQueryDTO orderQueryDTO = getOrderQueryDTO();
        OrderStatistic statistic = orderService.statistic(orderQueryDTO);
        Assert.assertEquals(4, statistic.getCount().intValue());
        Assert.assertEquals(3, statistic.getMoney().intValue());
    }

    @Test
    public void testFindFirst() {
        OrderQueryDTO orderQueryDTO = getOrderQueryDTO();
        OrderStatistic statistic = orderService.findFirst(orderQueryDTO);
        Assert.assertEquals("mongo", statistic.getStatisticName());
    }




    private OrderQueryDTO getOrderQueryDTO() {
        OrderQueryDTO orderQueryDTO = new OrderQueryDTO();
        orderQueryDTO.setLow(2);
        orderQueryDTO.setHigh(5);
        orderQueryDTO.setCreator("zhuojl");
        return orderQueryDTO;
    }

}