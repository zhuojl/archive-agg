package com.zhuojl.map.reduce.config;

import com.zhuojl.map.reduce.ArchiveKey;
import com.zhuojl.map.reduce.ArchiveKeyResolver;
import com.zhuojl.map.reduce.MapReduceAble;
import com.zhuojl.map.reduce.annotation.MapReduceMethodConfig;
import com.zhuojl.map.reduce.common.ArrayCloneUtil;
import com.zhuojl.map.reduce.common.enums.MapMode;
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
        return executeByMapReduce(method, args, sharding);
    }

    private Object executeByMapReduce(Method method, Object[] args, MapReduceMethodConfig sharding) {

        ArchiveKeyResolver archiveKeyResolver = map.get(sharding.paramHandler());
        ArchiveKey originalArchiveKey = archiveKeyResolver.extract(args);
        Reducer reducer = getReducer(sharding);

        if (MapMode.FIND_FIRST.equals(sharding.mapMode())) {
            return list.stream()
                    .map(item ->
                        doExecute(method, args, archiveKeyResolver, originalArchiveKey, item)
                    )
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        if (MapMode.ALL.equals(sharding.mapMode())) {
            return list.stream()
                    .map(item ->
                            doExecute(method, args, archiveKeyResolver, originalArchiveKey, item)
                    )
                    .filter(Objects::nonNull)
                    .reduce((obj1, obj2) -> reducer.reduce(obj1, obj2))
                    .orElse(null);
        }

        throw new UnsupportedOperationException("sth wrong");
    }

    /**
     * 执行请求
     * @param method
     * @param originalArgs 原始参数
     * @param archiveKeyResolver 归档参数处理器
     * @param originalArchiveKey 原始归档参数
     * @param target  invoke 的 target
     * @return
     */
    private Object doExecute(Method method, Object[] originalArgs,
                             ArchiveKeyResolver archiveKeyResolver,
                             ArchiveKey originalArchiveKey,
                             MapReduceAble target) {

        // 类配置与 原始归档参数 求交集
        ArchiveKey intersectionArchiveKey = target.intersectionArchiveKey(originalArchiveKey);
        // 类配置是否和查询有交集
        if (Objects.isNull(intersectionArchiveKey)) {
            return null;
        }

        // 根据归档键 交集 重构 方法参数
        Object[] params = archiveKeyResolver.rebuild(intersectionArchiveKey, ArrayCloneUtil.cloneParams(originalArgs));
        try {
            return method.invoke(target, params);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException ", e);
            throw new MyRuntimeException("c");
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException", e);
            throw new MyRuntimeException("d");
        }
    }


    private Reducer getReducer(MapReduceMethodConfig sharding) {
        String resultReducerBeanName = Strings.isBlank(sharding.reducer()) ?
                Reducer.DefaultReducer.BEAN_NAME : sharding.reducer();

        Reducer reducer = reduceMap.get(resultReducerBeanName);
        Objects.requireNonNull(reducer);
        return reducer;
    }

}