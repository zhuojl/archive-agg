package com.zhuojl.map.reduce.archivekey;

import java.lang.reflect.Method;

/**
 * 用于从方法中解析 ArchiveKey
 * @author zhuojl
 * @param <K>
 */
public interface ArchiveKeyResolver<K extends ArchiveKey> {

    /**
     * 从原始参数中抽取分段
     * @param params
     * @return
     */
    K extract(Method method, Object... params);


}
