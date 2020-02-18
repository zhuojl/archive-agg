package com.zhuojl.archive.agg;

import java.util.Objects;

public class IntegerAggregator implements Aggregator<Integer> {

    @Override
    public Integer aggregate(Integer t1, Integer t2) {
        if (Objects.isNull(t1)) {
            return t2;
        }
        if (Objects.isNull(t2)) {
            return t1;
        }
        return t1 + t2;
    }
}
