package com.zhuojl.archive.agg;

import java.util.Objects;

public class ForceReduceAbleAggregator implements Aggregator<ForceReduceAble> {

    @Override
    public ForceReduceAble aggregate(ForceReduceAble t1, ForceReduceAble t2) {
        if (Objects.isNull(t1)) {
            return t2;
        }
        if (Objects.isNull(t2)) {
            return t1;
        }

        t1.reduce(t2);

        return t1;
    }
}
