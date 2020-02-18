package com.zhuojl.archive.executor;

import java.util.Objects;

public class ArchiveFindFirstExecutor extends AbstractArchiveExecutor {


    public ArchiveFindFirstExecutor(ArchiveExecutorContext archiveExecutorContext) {
        super(archiveExecutorContext);
    }

    @Override
    public Object execute() {
        return this.context.getFilteredList().stream()
                .map(item ->
                        doExecute(context.getMethod(), item, this.context.getArgs())
                )
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

    }

}
