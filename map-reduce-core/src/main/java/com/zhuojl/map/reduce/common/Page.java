package com.zhuojl.map.reduce.common;

import java.util.List;
import java.util.Objects;

/**
 * copy from org.springframework.data.domain.Pageable
 *
 * @author zhuojl
 */
public class Page<T> {

    private List<T> data;
    private int pageNumber;
    private int pageSize;
    private int totalCount;



    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public int getStart() {

        return (pageNumber -1) * pageSize;
    }

    public int getLimit() {

        return pageSize;
    }

}
