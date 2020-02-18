package com.zhuojl.archive.executor;

import java.util.Objects;

public class ArchiveAllExecutor extends AbstractArchiveExecutor {


    public ArchiveAllExecutor(ArchiveExecutorContext archiveExecutorContext) {
        super(archiveExecutorContext);
    }

    @Override
    public Object execute() {
        return context.getFilteredList().stream()
                .map(item ->
                        doExecute(context.getMethod(), item, context.getArgs())
                )
                .filter(Objects::nonNull)
                .reduce((obj1, obj2) -> context.getAggregator().aggregate(obj1, obj2))
                .orElse(null);

    }

}
