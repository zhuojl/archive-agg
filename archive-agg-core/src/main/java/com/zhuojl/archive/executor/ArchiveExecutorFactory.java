package com.zhuojl.archive.executor;

import com.zhuojl.archive.common.enums.ExecuteMode;


/**
 * @author zhuojl
 */
public class ArchiveExecutorFactory {


    /**
     * 这样的判断，有点像把com.zhuojl.archive.annotation.ArchiveAggMethodConfig#executeMode()
     *
     * 改成配置 指定类了。 咱不处理
     */
    public static ArchiveExecutor getExecutorFactory(ExecuteMode executeMode, ArchiveExecutorContext context) {
        if (ExecuteMode.FIND_FIRST.equals(executeMode)) {
            return new ArchiveFindFirstExecutor(context);
        }

        if (ExecuteMode.ALL.equals(executeMode)) {
            return new ArchiveAllExecutor(context);
        }

        if (ExecuteMode.PAGE.equals(executeMode)) {
            return new PageArchiveExecutor(context);
        }

        throw new UnsupportedOperationException("strange execute mode or miss impl ");
    }
}
