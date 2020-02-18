package com.zhuojl.archive.agg;

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


}
