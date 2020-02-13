package com.zhuojl.map.reduce.config;

import com.zhuojl.map.reduce.ArchiveKey;
import com.zhuojl.map.reduce.ArchiveKeyResolver;
import com.zhuojl.map.reduce.MapReduceAble;
import com.zhuojl.map.reduce.annotation.MapReduceMethodConfig;
import com.zhuojl.map.reduce.common.exception.MyRuntimeException;
import com.zhuojl.map.reduce.reduce.Reducer;

import org.apache.logging.log4j.util.Strings;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

        ArchiveKeyResolver archiveKeyResolver = map.get(sharding.paramHandler());
        ArchiveKey originalArchiveKey = archiveKeyResolver.extract(args);
        Reducer reducer = getReducer(sharding);

        return list.stream()
                .map(item -> {

                    ArchiveKey intersectionArchiveKey = item.intersectionArchiveKey(archiveKeyResolver.extract(args));
                    if (Objects.isNull(intersectionArchiveKey)) {
                        return null;
                    }

                    // 根据归档键 交集 重构 方法参数
                    Object[] params = (Object[]) archiveKeyResolver.rebuild(intersectionArchiveKey, args);
                    try {
                        Object result = method.invoke(item, params);

                        // 根据 原始归档键 重构 方法参数
                        archiveKeyResolver.rebuild(originalArchiveKey, args);
                        return result;
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

    private Reducer getReducer(MapReduceMethodConfig sharding) {
        String resultReducerBeanName = Strings.isBlank(sharding.reduceBeanName()) ?
                Reducer.DefaultReducer.BEAN_NAME : sharding.reduceBeanName();

        Reducer reducer = reduceMap.get(resultReducerBeanName);
        Objects.requireNonNull(reducer);
        return reducer;
    }

}