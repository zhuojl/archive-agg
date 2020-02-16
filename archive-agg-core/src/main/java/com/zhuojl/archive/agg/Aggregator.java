package com.zhuojl.archive.agg;

import com.zhuojl.archive.common.ArchiveAggPage;
import com.zhuojl.archive.common.exception.MyRuntimeException;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 结果集归并
 *
 * @author zhuojl
 */
public interface Aggregator<T> {
    /**
     * 合并
     */
    T aggregate(T t1, T t2);

    @Component(DefaultAggregator.BEAN_NAME)
    class DefaultAggregator<T> implements Aggregator<T> {
        public static final String BEAN_NAME = "Aggregator.DefaultAggregator";

        @Override
        public T aggregate(T t1, T t2) {
            if (Objects.isNull(t2)) {
                return t1;
            }

            if (Objects.isNull(t1)) {
                return t2;
            }

            if (t1 instanceof Integer) {
                Integer t = (Integer) t1 + (Integer) t2;
                return (T) t;
            }

            if (t1 instanceof ForceReduceAble) {
                ForceReduceAble t = ((ForceReduceAble) t1).reduce((ForceReduceAble) t2);
                return (T) t;
            }

            if (t1 instanceof ArchiveAggPage) {
                return (T)mergeMapReducePage((ArchiveAggPage)t1,(ArchiveAggPage)t2);
            }

            if (t1 instanceof List) {

                return (T) reduceList((List) t1, (List) t2);
            }

            throw new MyRuntimeException("do sth");
        }

        private ArchiveAggPage mergeMapReducePage(ArchiveAggPage t1, ArchiveAggPage t2) {
            t1.setData(reduceList(t1.getData(), t2.getData()));
            return t1;
        }

        private List reduceList(List list1, List list2) {
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


}
