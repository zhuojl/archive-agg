package com.zhuojl.archive.demo.dto;

import com.zhuojl.archive.common.ArchiveAggPage;

import lombok.Data;

@Data
public class OrderPageDTO extends ArchiveAggPage<String> {

    Integer low;
    Integer high;

}
