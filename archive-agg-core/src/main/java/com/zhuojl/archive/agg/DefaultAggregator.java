package com.zhuojl.archive.agg;

import com.zhuojl.archive.common.ArchiveAggPage;
import com.zhuojl.archive.common.exception.MyRuntimeException;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认实现
 * @param <T>
 * @author zhuojl
 */
@Component(DefaultAggregator.BEAN_NAME)
public class DefaultAggregator<T> implements Aggregator<T> {
    public static final String BEAN_NAME = "Aggregator.DefaultAggregator";

    static Map<Class, Aggregator> defaultAggregatorMap = new ConcurrentHashMap<>();
    static Map<Class, Aggregator> customAggregatorMap = new ConcurrentHashMap<>();

    static {
        defaultAggregatorMap.put(Integer.class, new IntegerAggregator());
        defaultAggregatorMap.put(List.class, new ListAggregator());
        defaultAggregatorMap.put(ForceReduceAble.class, new ForceReduceAbleAggregator());
        defaultAggregatorMap.put(ArchiveAggPage.class, new ArchiveAggPageAggregator());
    }

    static {
        // put Custom Aggregator here
    }


    @Override
    public T aggregate(T t1, T t2) {
        if (Objects.isNull(t2)) {
            return t1;
        }

        if (Objects.isNull(t1)) {
            return t2;
        }

        Optional<Aggregator> optionalAggregator = getAggregator(t1);

        if (!optionalAggregator.isPresent()) {
            throw new MyRuntimeException("no aggregator founded for class: " + t1.getClass());
        }
        return (T) optionalAggregator.get().aggregate(t1, t2);

    }

    private Optional<Aggregator> getAggregator(T t1) {

        Optional<Aggregator> optionalAggregator = customAggregatorMap.entrySet()
                .stream()
                .filter(entry ->
                        entry.getKey().isAssignableFrom(t1.getClass())
                )
                .map(Map.Entry::getValue)
                .findFirst();
        if (optionalAggregator.isPresent()) {
            return optionalAggregator;
        }

        return defaultAggregatorMap.entrySet()
                .stream()
                .filter(entry ->
                        entry.getKey().isAssignableFrom(t1.getClass())
                )
                .map(Map.Entry::getValue)
                .findFirst();
    }

}