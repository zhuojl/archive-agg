package com.zhuojl.share.compose;


/**
 * 分片接口，
 * <p>
 * XXX 本来是想通过注解中配置枚举来做，但是注解配置在接口中，无法识别子类！！
 * @author zhuojl
 */
public interface ComposeAble<P extends ComposeParam> {

    /**
     * 获取当前段 执行的分段参数
     * @param composeParam
     * @return
     */
    P getExecuteParam(P composeParam);


}
