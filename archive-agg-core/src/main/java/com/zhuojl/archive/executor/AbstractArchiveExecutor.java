package com.zhuojl.archive.executor;

import com.zhuojl.archive.ArchiveAggAble;
import com.zhuojl.archive.common.exception.MyRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractArchiveExecutor implements ArchiveExecutor{

    protected ArchiveExecutorContext context;

    public AbstractArchiveExecutor(ArchiveExecutorContext context) {
        this.context = context;
    }


    /**
     * 执行请求
     *
     * @param archiveAggAble      invoke 的 target
     * @param executedParams     原始参数
     */
    protected Object doExecute(Method method, ArchiveAggAble archiveAggAble, Object[] executedParams) {

        try {
            return method.invoke(archiveAggAble, executedParams);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException ", e);
            throw new MyRuntimeException("c");
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException", e);
            throw new MyRuntimeException("d");
        }


    }

}
