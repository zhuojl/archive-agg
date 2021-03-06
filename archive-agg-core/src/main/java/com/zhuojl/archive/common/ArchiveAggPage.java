package com.zhuojl.archive.common;

import java.util.Objects;

/**
 * 似乎有些做不了分页，如果考虑任意排序，就需解析排序规则，不仅如此，还需要处理内存分页的问题，
 *
 * 根据项目中实际的处理经验来看，分页时，归档数据会根据冷热区分优先级，优先返回热数据，再查询冷数据。
 *
 * 这样解决内存分野的情况，也不用考虑排序的配置解析等。
 *
 * 其中一种实现方式就是: 返回各种归档区间数据的数量，再根据分页要求调整分页条件，分别索取对应区间的数据。
 *
 * 由于pageNumber和pageSize很难调整，且在实际项目中，sql真正使用的其实是start和limit，
 *
 * 所以这里提供直接调整start和limit的方法
 *
 * @author zhuojl
 */
public class ArchiveAggPage<T> extends Page<T> {


    private int start;

    private int limit;

    @Override
    public int getStart() {
        if (Objects.nonNull(start)) {
            return start;
        }
        return super.getStart();
    }

    @Override
    public int getLimit() {
        if (Objects.nonNull(limit)) {
            return limit;
        }
        return super.getLimit();
    }


    public void setStart(int start) {
        this.start = start;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }



}
