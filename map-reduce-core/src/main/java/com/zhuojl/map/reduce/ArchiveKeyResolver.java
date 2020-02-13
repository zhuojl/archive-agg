package com.zhuojl.map.reduce;

/**
 * 组合执行参数处理器，作用是为在之前的应用场景中，每个分段的有数据重合，需要重组参数，避免查询到重复数据。
 * @author zhuojl
 * @param <A>
 */
public interface ArchiveKeyResolver<A extends ArchiveKey> {

    /**
     * 从原始参数中抽取分段
     * @param params
     * @return
     */
    ParamWithArchiveKey<A> extract(Object... params);

    /**
     * 根据分段参数重构原始参数
     *
     * @param paramWithArchiveKey
     * @return
     */
    Object rebuild(ParamWithArchiveKey<A> paramWithArchiveKey);

}
