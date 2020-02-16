package com.zhuojl.map.reduce.common;

import com.zhuojl.map.reduce.common.exception.MyRuntimeException;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArrayCloneUtil {

    public static Object[] cloneParams(Object... params) {

        Object[] arr = new Object[params.length];
        try {
            for (int i = 0; i < arr.length; i++) {
                // 基本数据类型直接赋值
                if (skipClone(params[i])) {
                    arr[i] = params[i];
                } else {
                    arr[i] = BeanUtils.cloneBean(params[i]);
                }
            }
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException ", e);
            throw new MyRuntimeException("IllegalAccessException");
        } catch (InstantiationException e) {
            log.error("InstantiationException ", e);
            throw new MyRuntimeException("InstantiationException");
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException ", e);
            throw new MyRuntimeException("InvocationTargetException");
        } catch (NoSuchMethodException e) {
            log.error("NoSuchMethodException ", e);
            throw new MyRuntimeException("NoSuchMethodException");
        }
        return arr;
    }

    private static boolean skipClone(Object o) {
        return o instanceof Number || o instanceof Boolean || o instanceof Character;
    }
}
