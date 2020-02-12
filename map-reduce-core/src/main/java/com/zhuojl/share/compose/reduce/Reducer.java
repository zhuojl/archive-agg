package com.zhuojl.share.compose.reduce;

import com.zhuojl.share.compose.common.ForceReduceAble;
import com.zhuojl.share.compose.common.ReduceAble;
import com.zhuojl.share.compose.common.exception.MyRuntimeException;

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
public interface Reducer<T> {
    /**
     * 合并
     */
    T reduce(T t1, T t2);

    @Component(DefaultReducer.BEAN_NAME)
    class DefaultReducer<T> implements Reducer<T> {
        public static final String BEAN_NAME = "Reducer.DefaultReducer";

        @Override
        public T reduce(T t1, T t2) {
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

            if (t1 instanceof List) {

                return (T) reduceList((List) t1, (List) t2);
            }

            throw new MyRuntimeException("do sth");
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
                List<ReduceAble> list =
                        reduceAbleList
                                .stream()
                                .collect(Collectors.groupingBy(ReduceAble::getMergeKey,
                                        Collectors.reducing(ReduceAble::reduce)))
                                .values()
                                .stream()
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList());

                return list;
            }

            return list1;
        }

    }


}
