package com.zhuojl.map.reduce.archivekey;


/**
 * 归档键
 * @author zhuojl
 */
public interface ArchiveKey<K extends ArchiveKey> {

    /**
     * 与目标归档键 取交集
     * @param config
     * @return
     */
    K intersection(K config);

    /**
     * 归档键 的 优先级
     * @return
     */
    int getOrder();
}
