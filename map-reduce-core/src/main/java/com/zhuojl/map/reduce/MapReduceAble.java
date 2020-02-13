package com.zhuojl.map.reduce;


/**
 * 分片接口，
 * <p>
 * XXX 本来是想通过注解中配置枚举来做，但是注解配置在接口中，无法识别子类！！
 * @author zhuojl
 */
public interface MapReduceAble<K extends ArchiveKey> {

    /**
     * 每个配置的归档键
     */
    K getArchiveKey();

//    /**
//     * 每个实现的归档键与查询归档键求交集
//     * @param queryArchiveKey 具体方法查询时的 归档键
//     * @return 一个新的归档参数和归档配置
//     */
//    K intersectionArchiveKey(K queryArchiveKey);

    /**
     * 每个实现的归档键与查询归档键求交集
     */
    default K intersectionArchiveKey(K queryArchiveKey) {
        return (K)getArchiveKey().intersection(queryArchiveKey);
    }

}
