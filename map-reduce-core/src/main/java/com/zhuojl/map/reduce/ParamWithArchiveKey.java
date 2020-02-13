package com.zhuojl.map.reduce;

import lombok.Builder;
import lombok.Data;

/**
 * @author zhuojl
 */
@Data
@Builder
public class ParamWithArchiveKey<A extends ArchiveKey> {

    private Object[] originalParams;

    private A archiveKey;

}
