package com.zhuojl.share.compose.demo.model;

import com.zhuojl.share.compose.common.ForceReduceAble;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author zhuojl
 */
@Data
@AllArgsConstructor
public class OrderStatistic implements ForceReduceAble<OrderStatistic> {

    private String statisticName;
    private Integer money;
    private Integer count;


    @Override
    public OrderStatistic reduce(OrderStatistic reduce) {
        if (Objects.isNull(reduce)) {
            return this;
        }
        sumMoney(reduce);
        sumCount(reduce);
        return this;
    }

    private void sumCount(OrderStatistic reduce) {
        if (Objects.isNull(this.count)) {
            this.count = reduce.count;
        }
        this.count = this.count + reduce.count;
    }

    private void sumMoney(OrderStatistic reduce) {
        if (Objects.isNull(this.money)) {
            this.money = reduce.money;
        }
        this.money = this.money + reduce.money;
    }


}
