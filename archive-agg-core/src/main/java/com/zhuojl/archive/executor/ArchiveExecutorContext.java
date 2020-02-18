package com.zhuojl.archive.executor;

import com.zhuojl.archive.ArchiveAggAble;
import com.zhuojl.archive.agg.Aggregator;

import java.lang.reflect.Method;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 执行上下文
 *
 * @author zhuojl
 */
@Builder
@Setter
@Getter
public class ArchiveExecutorContext {

    private Method method;
    private Object[] args;
    private Aggregator aggregator;
    private List<ArchiveAggAble> filteredList;
}
