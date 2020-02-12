package com.zhuojl.share.compose.common;

/**
 * 可merge的
 * @author zhuojl
 * @param <T>
 */
public interface MergeAble<T extends MergeAble> {

    /**
     * 是否可merge
     * @param mergeAble
     * @return
     */
    boolean canMerge(T mergeAble);

    /**
     * 合并
     * @param mergeAble
     * @return
     */
    MergeAble merge(T mergeAble);
}
