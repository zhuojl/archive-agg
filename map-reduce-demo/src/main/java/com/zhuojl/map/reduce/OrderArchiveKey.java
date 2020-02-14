package com.zhuojl.map.reduce;

import com.google.common.collect.Range;

import com.zhuojl.map.reduce.archivekey.ArchiveKey;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 订单归档规则，假定归档数字区间归档，其实这里按日期归档一样的。
 *
 * @author zhuojl
 */
@Data
@AllArgsConstructor
public class OrderArchiveKey implements ArchiveKey<OrderArchiveKey> {

    private Range<Integer> range;


    @Override
    public OrderArchiveKey intersection(OrderArchiveKey archiveKey) {
        if (Objects.isNull(archiveKey) || Objects.isNull(archiveKey.getRange())) {
            return null;
        }

        Range<Integer> intersection = this.range.intersection(archiveKey.getRange());

        return new OrderArchiveKey(intersection);
    }
}
