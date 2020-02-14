package com.zhuojl.map.reduce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MapMode {

    /**
     * find first, 适用于详情类的接口，先从热库中查询。
     */
    FIND_FIRST,

    /**
     * 返回所有符合条件的分段，列表，汇总等
     */
    ALL


}
