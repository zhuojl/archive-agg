package com.zhuojl.map.reduce.dto;

import com.zhuojl.map.reduce.common.MapReducePage;

import lombok.Data;

@Data
public class OrderPageDTO extends MapReducePage<String> {

    Integer low;
    Integer high;

}
