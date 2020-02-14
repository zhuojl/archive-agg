package com.zhuojl.map.reduce.config;

import com.zhuojl.map.reduce.ArchiveKey;
import com.zhuojl.map.reduce.ArchiveKeyResolver;
import com.zhuojl.map.reduce.MapReduceAble;
import com.zhuojl.map.reduce.annotation.MapReduceMethodConfig;
import com.zhuojl.map.reduce.common.exception.MyRuntimeException;
import com.zhuojl.map.reduce.reduce.Reducer;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.util.Strings;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapReduceProxy implements InvocationHandler {

    private final List<MapReduceAble> list;
    private final Map<String, ArchiveKeyResolver> map;
    private final Map<String, Reducer> reduceMap;

    public MapReduceProxy(List<MapReduceAble> list, Map<String, ArchiveKeyResolver> map, Map<String, Reducer> reduceMap) {
        this.list = list;
        this.map = map;
        this.reduceMap = reduceMap;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {

        MapReduceMethodConfig sharding = method.getAnnotation(MapReduceMethodConfig.class);
        if (Objects.isNull(sharding) || !map.containsKey(sharding.paramHandler())
                || Objects.isNull(args) || args.length == 0) {
            try {
                return method.invoke(proxy, args);
            } catch (IllegalAccessException e) {
                throw new MyRuntimeException("a");
            } catch (InvocationTargetException e) {
                throw new MyRuntimeException("b");
            }
        }

        log.info("invoke class: {}, method: {}", proxy.getClass().getSimpleName(), method.getName());
        return executeByMapReduce(method, args, sharding);
    }

    private Object executeByMapReduce(Method method, Object[] args, MapReduceMethodConfig sharding) {

        ArchiveKeyResolver archiveKeyResolver = map.get(sharding.paramHandler());
        ArchiveKey originalArchiveKey = archiveKeyResolver.extract(args);
        Reducer reducer = getReducer(sharding);

        return list.stream()
                .map(item -> {

                    ArchiveKey intersectionArchiveKey = item.intersectionArchiveKey(originalArchiveKey);
                    if (Objects.isNull(intersectionArchiveKey)) {
                        return null;
                    }

                    // 根据归档键 交集 重构 方法参数
                    Object[] params = archiveKeyResolver.rebuild(intersectionArchiveKey, cloneParams(args));
                    try {
                        return method.invoke(item, params);
                    } catch (IllegalAccessException e) {
                        log.error("IllegalAccessException ", e);
                        throw new MyRuntimeException("c");
                    } catch (InvocationTargetException e) {
                        log.error("InvocationTargetException", e);
                        throw new MyRuntimeException("d");
                    }

                })
                .filter(Objects::nonNull)
                .reduce((obj1, obj2) -> reducer.reduce(obj1, obj2))
                .orElse(null);
    }


    private Object[] cloneParams(Object... params) {

        Object[] arr = Arrays.copyOf(params, params.length);
            try {
                for (int i = 0; i < arr.length; i++) {
                    // 基本数据类型直接赋值
                    if (skipClone(arr[i])) {
                        arr[i] = params[i];
                    } else {
                        arr[i] = BeanUtils.cloneBean(arr[i]);
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

    private boolean skipClone(Object o) {
        return o instanceof Number || o instanceof Boolean || o instanceof Character;
    }

    private Reducer getReducer(MapReduceMethodConfig sharding) {
        String resultReducerBeanName = Strings.isBlank(sharding.reducer()) ?
                Reducer.DefaultReducer.BEAN_NAME : sharding.reducer();

        Reducer reducer = reduceMap.get(resultReducerBeanName);
        Objects.requireNonNull(reducer);
        return reducer;
    }

}