package com.zhuojl.map.reduce.reduce;

/**
 * 可merge的,需要确保返回结果不为null，不然抛NPE
 *
 * 用于只返回一个结果，内部有多个内容需要合并的场景
 *
 * @author zhuojl
 * @param <T>
 */
public interface ForceReduceAble<T extends ForceReduceAble> {

    /**
     * 合并
     * @param reduceAble
     * @return
     */
    ForceReduceAble reduce(T reduceAble);
}
