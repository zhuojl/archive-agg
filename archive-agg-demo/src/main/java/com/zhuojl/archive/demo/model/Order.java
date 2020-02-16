package com.zhuojl.archive.demo.model;

import lombok.Builder;
import lombok.Data;

/**
 * 订单实体
 *
 * @author zhuojl
 */
@Data
@Builder
public class Order {
    private Long orderId;
    private Long amount;
    private String creator;
}
