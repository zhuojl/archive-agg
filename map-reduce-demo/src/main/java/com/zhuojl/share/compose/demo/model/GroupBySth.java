package com.zhuojl.share.compose.demo.model;

import com.zhuojl.share.compose.common.ReduceAble;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author zhuojl
 */
@Data
@AllArgsConstructor
public class GroupBySth implements ReduceAble<GroupBySth> {

    private Integer month;

    private Integer sum;

    @Override
    public Object getMergeKey() {
        Objects.requireNonNull(month);
        return month;
    }

    @Override
    public GroupBySth reduce(GroupBySth reduce) {
        if (Objects.isNull(reduce)) {
            return this;
        }
        sumSum(reduce);
        return this;
    }

    private void sumSum(GroupBySth reduce) {
        if (Objects.isNull(this.sum)) {
            this.sum = reduce.sum;
        }
        this.sum = this.sum + reduce.sum;
    }

}
