package com.zhuojl.archive.agg;

import com.zhuojl.archive.common.ArchiveAggPage;

import java.util.Objects;

public class ArchiveAggPageAggregator implements Aggregator<ArchiveAggPage> {

    @Override
    public ArchiveAggPage aggregate(ArchiveAggPage t1, ArchiveAggPage t2) {
        if (Objects.isNull(t1)) {
            return t2;
        }
        if (Objects.isNull(t2)) {
            return t1;
        }

        t1.setData(new ListAggregator().aggregate(t1.getData(), t2.getData()));

        return t1;
    }
}
