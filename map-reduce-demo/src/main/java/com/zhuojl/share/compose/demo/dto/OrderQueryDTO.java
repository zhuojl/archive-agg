package com.zhuojl.share.compose.demo.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 订单查询实体
 */
@Data
@ToString
public class OrderQueryDTO {

    String creator;
    Integer low;
    Integer high;
}
