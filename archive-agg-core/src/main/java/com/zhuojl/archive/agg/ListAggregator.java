package com.zhuojl.archive.agg;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListAggregator implements Aggregator<List> {

    @Override
    public List aggregate(List list1, List list2) {
        if (CollectionUtils.isEmpty(list1)) {
            return list2;
        }

        if (CollectionUtils.isEmpty(list2)) {
            return list1;
        }

        list1.addAll(list2);
        // 如果可以合并
        if (list1.get(0) instanceof ReduceAble) {

            List<ReduceAble> reduceAbleList = (List<ReduceAble>) list1;
            return reduceAbleList
                    .stream()
                    .collect(Collectors.groupingBy(ReduceAble::getMergeKey,
                            Collectors.reducing(ReduceAble::reduce)))
                    .values()
                    .stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

        }

        return list1;
    }
}
