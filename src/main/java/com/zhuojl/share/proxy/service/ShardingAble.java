package com.zhuojl.share.proxy.service;

import com.zhuojl.share.proxy.common.basic.TimeRange;

import java.util.Date;

/**
 * 分片接口，
 * <p>
 * XXX 本来是想通过注解中配置枚举来做，但是注解配置在接口中，无法识别子类！！
 */
public interface ShardingAble {

    /**
     * 查询分片时间， 如果返回空，表示不走该分片
     * 这样做不到一个方法一个分区策略，但是正常情况一个类就应该是同一个分区策略，不同策略说明数据存储都不一致，不一致正常应该可以拆分开
     *
     * @return
     */
    TimeRange getTimeRange(Date startTime, Date endTime);
}
