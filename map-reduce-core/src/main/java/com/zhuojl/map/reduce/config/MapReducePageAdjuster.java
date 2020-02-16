package com.zhuojl.map.reduce.config;

import com.zhuojl.map.reduce.common.ArrayCloneUtil;
import com.zhuojl.map.reduce.common.MapReducePage;
import com.zhuojl.map.reduce.common.enums.ExecuteMode;

import java.util.Objects;

/**
 * 在 {@link ExecuteMode#PAGE}模式下， 需要做分页调整，
 * @author zhuojl
 */
public class MapReducePageAdjuster {

    public MapReducePageAdjuster(MapReducePage originalPage) {
        Objects.requireNonNull(originalPage);
        this.originalPage = originalPage;
        this.originalPageSize = originalPage.getPageSize();
        this.originalPageNumber = originalPage.getPageNumber();
        this.originalStart = originalPage.getStart();
        this.originalLimit = originalPage.getLimit();
        this.startIndex = (this.originalPageNumber - 1) * this.originalPageSize;

    }

    private final MapReducePage originalPage;
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
