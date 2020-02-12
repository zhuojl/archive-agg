package com.zhuojl.share.compose.common.enums;

import com.zhuojl.share.compose.common.MergeAble;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 结果集合并策略，当group by不是按分片键group by的，
 *
 * 1、需要查询列表在应用中分组
 * 2、先分组，再按分组键合并
 *
 * @author zhuojl
 */
public enum MergeEnum {

    NONE() {
        @Override
        public Object merge(Object obj1, Object obj2) {
            return null;
        }
    },

    /**
     * 简单集合合并
     */
    SIMPLE_LIST() {
        @Override
        public Object merge(Object obj1, Object obj2) {
            List list1 = (List) obj1;
            List list2 = (List) obj2;
            if (CollectionUtils.isEmpty(list1)) {
                return list2;
            }

            if (CollectionUtils.isEmpty(list2)) {
                return list1;
            }
            // 如果可以合并
            if (list1.get(0) instanceof MergeAble) {

                List<MergeAble> mergeAbleList = (List<MergeAble>) list1;
                Map<MergeAble, MergeAble> mergeAbleMap = mergeAbleList.stream()
                        .collect(Collectors.toMap(e -> e, e -> e));

                for (Object obj : list2) {
                    MergeAble mergeAble = (MergeAble) obj;
                    mergeAbleMap.merge(mergeAble, mergeAble, (o, n) -> o.merge(n));
                }
                return mergeAbleMap.keySet().stream().collect(Collectors.toList());
            } else {
                list1.addAll(list2);
                return list1;
            }
        }
    },

    /**
     * 简单数字合并
     */
    SIMPLE_NUMBER() {
        @Override
        public Object merge(Object obj1, Object obj2) {
            if (Objects.isNull(obj1)) {
                return obj2;
            }

            if (Objects.isNull(obj2)) {
                return obj1;
            }
            // 数字类型
            if (obj1 instanceof Integer && obj2 instanceof Integer) {
                return (Integer) obj1 + (Integer) obj2;
            }
            return obj1;
        }
    },

    /**
     * 合并对象
     */
    SIMPLE_OBJECT() {
        @Override
        public Object merge(Object obj1, Object obj2) {
            if (Objects.isNull(obj1)) {
                return obj2;
            }

            if (Objects.isNull(obj2)) {
                return obj1;
            }
            if (obj1 instanceof MergeAble && obj2 instanceof MergeAble) {
                return ((MergeAble) obj1).merge((MergeAble) obj2);
            }
            return obj1;
        }
    };

    public abstract Object merge(Object obj1, Object obj2);



}
