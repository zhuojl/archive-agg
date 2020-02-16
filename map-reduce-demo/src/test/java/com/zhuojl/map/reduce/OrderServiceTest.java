package com.zhuojl.map.reduce;

import com.zhuojl.map.reduce.dto.OrderPageDTO;
import com.zhuojl.map.reduce.dto.OrderQueryDTO;
import com.zhuojl.map.reduce.model.GroupBySth;
import com.zhuojl.map.reduce.model.OrderStatistic;
import com.zhuojl.map.reduce.service.order.OrderService;

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
        Assert.assertEquals(6, count.intValue());
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


    @Test
    public void testPageInSecondPage() {
        OrderPageDTO orderPageDTO = new OrderPageDTO();
        orderPageDTO.setLow(7);
        orderPageDTO.setHigh(16);
        orderPageDTO.setPageNumber(2);
        orderPageDTO.setPageSize(3);
        /*
         * OrderService6个实现的归档区间[1,15], 根据[7,16]查询，返回结果为[7,15]，总数9
         * MySqlOrderServiceImpl1区间[4,7]，其他每个依此往上占有2个，即[8,9]、[10,11]、[12,13]、[14,15]
         * 3个一页，查询第二页时，返回12,11,10
         */
        OrderPageDTO page = orderService.page(orderPageDTO);
        Assert.assertNotNull(page);
        Assert.assertEquals(9, page.getTotalCount());
        Assert.assertEquals(3, page.getData().size());
        Assert.assertEquals("index:12", page.getData().get(0));
        Assert.assertEquals("index:11", page.getData().get(1));
        Assert.assertEquals("index:10", page.getData().get(2));
    }

    @Test
    public void testPageInFirstPage() {
        OrderPageDTO orderPageDTO = new OrderPageDTO();
        orderPageDTO.setLow(1);
        orderPageDTO.setHigh(2);
        orderPageDTO.setPageNumber(1);
        orderPageDTO.setPageSize(3);
        /*
         * OrderService6个实现的归档区间[1,15], 根据[7,16]查询，返回结果为[7,15]，总数9
         * MySqlOrderServiceImpl1区间[4,7]，其他每个依此往上占有2个，即[8,9]、[10,11]、[12,13]、[14,15]
         * 3个一页，查询第二页时，返回12,11,10
         */
        OrderPageDTO page = orderService.page(orderPageDTO);
        Assert.assertNotNull(page);
        Assert.assertEquals(2, page.getTotalCount());
        Assert.assertEquals(2, page.getData().size());
    }

    @Test
    public void testPageEmpty() {
        OrderPageDTO orderPageDTO = new OrderPageDTO();
        orderPageDTO.setLow(111);
        orderPageDTO.setHigh(222);
        orderPageDTO.setPageNumber(1);
        orderPageDTO.setPageSize(3);
        /*
         * OrderService6个实现的归档区间[1,15], 根据[7,16]查询，返回结果为[7,15]，总数9
         * MySqlOrderServiceImpl1区间[4,7]，其他每个依此往上占有2个，即[8,9]、[10,11]、[12,13]、[14,15]
         * 3个一页，查询第二页时，返回12,11,10
         */
        OrderPageDTO page = orderService.page(orderPageDTO);
        Assert.assertNotNull(page);
        Assert.assertEquals(0, page.getTotalCount());
        Assert.assertEquals(0, page.getData().size());
    }


    private OrderQueryDTO getOrderQueryDTO() {
        OrderQueryDTO orderQueryDTO = new OrderQueryDTO();
        orderQueryDTO.setLow(2);
        orderQueryDTO.setHigh(5);
        orderQueryDTO.setCreator("zhuojl");
        return orderQueryDTO;
    }

}