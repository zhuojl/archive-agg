package com.zhuojl.archive.executor;

import com.zhuojl.archive.ArchiveAggAble;
import com.zhuojl.archive.common.ArchiveAggPage;
import com.zhuojl.archive.common.ArrayCloneUtil;
import com.zhuojl.archive.common.enums.ExecuteMode;
import com.zhuojl.archive.common.exception.MyRuntimeException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PageArchiveExecutor extends AbstractArchiveExecutor {
    public static final String COUNT = "Count";

    public PageArchiveExecutor(ArchiveExecutorContext archiveExecutorContext) {
        super(archiveExecutorContext);
    }

    @Override
    public Object execute() {
        ArchiveAggPage archiveAggPage = getMapReducePage(context.getArgs());
        Objects.requireNonNull(archiveAggPage, "mapReducePage must be not null");
        if (context.getMethod().getReturnType().isAssignableFrom(ArchiveAggPage.class)) {
            throw new MyRuntimeException("page method with error return type");
        }

        // 便利执行 计数方法 返回 类全名，每区块计数器
        Map<ArchiveAggAble, Integer> countMap = getCountMap();
        Integer count = countMap.values().stream().reduce(Integer::sum).orElse(0);
        if (count == 0) {
            // XXX 如果没有则返回 reduce查询原始对象/或者clone对象，当然这样可能会有问题
            return archiveAggPage;
        }

        // 执行结果
        Map<ArchiveAggAble, Object> resultMap = getResultMap(archiveAggPage, countMap);

        Object obj = context.getFilteredList().stream()
                .filter(item -> countMap.containsKey(item) && resultMap.containsKey(item))
                .map(item -> resultMap.get(item))
                .reduce((obj1, obj2) -> context.getAggregator().aggregate(obj1, obj2))
                .orElse(null);

        if (Objects.isNull(obj)) {
            // XXX 如果没有则返回 reduce查询原始对象/或者clone对象，当然这样可能会有问题
            return archiveAggPage;
        }

        ((ArchiveAggPage) obj).setTotalCount(count);
        ((ArchiveAggPage) obj).setPageNumber(archiveAggPage.getPageNumber());
        ((ArchiveAggPage) obj).setPageSize(archiveAggPage.getPageSize());
        return obj;

    }

    private ArchiveAggPage getMapReducePage(Object[] args) {
        ArchiveAggPage archiveAggPage = null;
        for (Object obj : args) {
            if (obj instanceof ArchiveAggPage) {
                archiveAggPage = (ArchiveAggPage) obj;
            }
        }
        return archiveAggPage;
    }

    private Map<ArchiveAggAble, Integer> getCountMap() {

        Method countMethod;
        try {
            Class declaringClass = context.getMethod().getDeclaringClass();
            String countMethodName = context.getMethod().getName() + COUNT;
            countMethod = declaringClass.getMethod(countMethodName,
                    context.getMethod().getParameterTypes());
        } catch (NoSuchMethodException e) {
            log.error("NoSuchMethodException", e);
            throw new MyRuntimeException("page count method is needed for this method");
        }

        Map<ArchiveAggAble, Integer> countMap = new HashMap<>(context.getFilteredList().size());
        for (ArchiveAggAble item : context.getFilteredList()) {
            Object count = doExecute(countMethod, item, context.getArgs());
            countMap.put(item, Objects.isNull(count) ? 0 : (Integer) count);
        }
        return countMap;

    }

    private Map<ArchiveAggAble, Object> getResultMap(ArchiveAggPage archiveAggPage,
                                                     Map<ArchiveAggAble, Integer> countMap) {

        Map<ArchiveAggAble, Object> resultMap = new HashMap<>();

        // adjust Page Params(start, limit)
        ArchiveAggPageAdjuster archiveAggPageAdjuster = new ArchiveAggPageAdjuster(archiveAggPage);

        for (ArchiveAggAble archiveAggAble : context.getFilteredList()) {

            Object[] adjustParam = archiveAggPageAdjuster.adjustParam(countMap.get(archiveAggAble), context.getArgs());

            if (Objects.isNull(adjustParam)) {
                continue;
            }
            Object result = doExecute(context.getMethod(), archiveAggAble, adjustParam);
            resultMap.put(archiveAggAble, result);
        }

        return resultMap;
    }

    /**
     * 在 {@link ExecuteMode#PAGE}模式下， 需要做分页参数调整
     *
     * @author zhuojl
     */
    class ArchiveAggPageAdjuster {

        public ArchiveAggPageAdjuster(ArchiveAggPage originalPage) {
            Objects.requireNonNull(originalPage);
            this.originalPage = originalPage;
            this.originalPageSize = originalPage.getPageSize();
            this.originalPageNumber = originalPage.getPageNumber();
            this.originalStart = originalPage.getStart();
            this.originalLimit = originalPage.getLimit();
            this.startIndex = (this.originalPageNumber - 1) * this.originalPageSize;

        }

        private final ArchiveAggPage originalPage;
        private final int originalPageSize;
        private final int originalPageNumber;
        private final int originalStart;
        private final int originalLimit;
        private final Integer startIndex;
        // 占用的数量
        Integer usedCount = 0;
        // 便利完的数字
        Integer passedCount = 0;


        /**
         * 通过clone改过分页属性的MapReducePage，来返回需要的参数
         *
         * 依此传入0，6，0，6，6，0，6 的数量队列，每页10个，取第二页，则取0，0，0，2（后2），6，0，2（前2）
         *
         * @param count 每段的数量
         */
        public Object[] adjustParam(Integer count, Object[] originalParams) {
            if (Objects.isNull(count) || count == 0) {
                return null;
            }

            passedCount += count;

            // 表示抽取的数量已经 和 一页的大小相同
            if (usedCount == originalPageSize ||
                    // 未达到 起始取值点
                    passedCount <= startIndex) {
                return null;
            }

            // 只有第一个区间的开始不是0，其他区间的开始都是0
            if (usedCount == 0) {
                // 开始的位置，那上面的例子来说， 就是第二个6的的第5个 (10 - 12 + 6)
                originalPage.setStart(startIndex - passedCount + count);
            } else {
                originalPage.setStart(0);
            }
            // 实际只取了start 到总数末尾那么多个，即 count - start；并且不能大于剩余的需求，虽然大于也无所谓，但是这样 计算used就会有问题
            originalPage.setLimit(Math.min(count - originalPage.getStart(), originalPageSize - usedCount));

            usedCount += originalPage.getLimit();

            Object[] clonedParams = ArrayCloneUtil.cloneParams(originalParams);
            // 恢复参数设置
            originalPage.setStart(this.originalStart);
            originalPage.setLimit(this.originalLimit);

            return clonedParams;
        }
    }

}
