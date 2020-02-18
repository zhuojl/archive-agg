package com.zhuojl.archive;


import com.zhuojl.archive.archivekey.ArchiveKey;

/**
 * 归档接口，
 * <p>
 * XXX 本来是想通过注解中配置枚举来做，但是注解配置在接口中，无法识别子类！！
 * @author zhuojl
 */
public interface ArchiveAggAble<K extends ArchiveKey> {

    /**
     * 每个配置的归档键
     */
    K getArchiveKey();

    /**
     * 每个实现的归档键与查询归档键求交集
     */
    default K intersectionArchiveKey(K queryArchiveKey) {
        return (K)getArchiveKey().intersection(queryArchiveKey);
    }

}
