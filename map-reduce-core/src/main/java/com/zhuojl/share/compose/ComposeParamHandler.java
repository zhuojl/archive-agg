package com.zhuojl.share.compose;

/**
 * 组合执行参数处理器，作用是为在之前的应用场景中，每个分段的有数据重合，需要重组参数，避免查询到重复数据。
 * @author zhuojl
 * @param <P>
 */
public interface ComposeParamHandler<P extends ComposeParam> {

    /**
     * 从原始参数中抽取分段
     * @param params
     * @return
     */
    P extract(Object... params);

    /**
     * 根据分段参数重构原始参数
     *
     * @param composeParam
     * @return
     */
    Object rebuild(P composeParam);

}
