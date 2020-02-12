package com.zhuojl.map.reduce.common;

/**
 * 可merge的
 * @author zhuojl
 * @param <T>
 */
public interface ReduceAble<T extends ReduceAble> extends ForceReduceAble<T> {

    /**
     * 获取一个对象用于合并判断的key，即合并纬度
     * @return
     */
    Object getMergeKey();

    /**
     * 合并
     * @param reduce
     * @return
     */
    @Override
    ReduceAble reduce(T reduce);

}
