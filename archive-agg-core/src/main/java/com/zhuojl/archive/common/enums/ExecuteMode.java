package com.zhuojl.archive.common.enums;

import com.zhuojl.archive.config.ArchiveAggProxy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExecuteMode {

    /**
     * find first, 适用于详情类的接口，先从热库中查询。
     */
    FIND_FIRST,

    /**
     * 返回所有符合条件的分段，列表，汇总等
     */
    ALL,

    /**
     * 分页模式, 这是一种分页的集成，对方法定义有严格的格式要求，如果不想遵从这些约定，就需要在业务逻辑中组装。
     *
     * 1、至少需要一个MapReducePage的参数，方法的返回也必须是MapReducePage，
     * 2、copy上分页方法签名，返回类型改为Integer，方法名尾部加 {@link ArchiveAggProxy#COUNT}。
     * （注意是public，代码中暂时没做访问private处理）
     * 3、如果查询区间与归档区间没有交集，返回查询参数中的MapReducePage
     *
     * 执行逻辑：（假设：分页方法page，count方法pageCount）
     *
     * 1、便利执行pageCount，返回各个区段数据数量，返回类型map，是为了支持内部多线程执行
     * 2、便利执行page，返回各个区段数据，返回类型是map，是为了支持多线程执行。 在这步里，有做分页参数调节
     * 3、根据1、2结果集按顺序组装，返回
     *
     */
    PAGE


}
