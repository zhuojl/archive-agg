package com.zhuojl.share.proxy.common.enums;

import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;

public enum BinaryOperatorEnum {

    LIST_MERGE {
        @Override
        public BinaryOperator getListBinaryOperator() {
            return (obj1, obj2) -> {
                if (Objects.isNull(obj1)) {
                    return obj2;
                }
                if (Objects.isNull(obj2)) {
                    return obj1;
                }
                ((List) obj1).addAll((List) obj2);
                return obj1;
            };
        }
    };

    public abstract BinaryOperator getListBinaryOperator();
}
