package com.zhuojl.map.reduce.config;

import com.zhuojl.map.reduce.MapReduceAble;
import com.zhuojl.map.reduce.annotation.MapReduceMethodConfig;
import com.zhuojl.map.reduce.archivekey.ArchiveKey;
import com.zhuojl.map.reduce.archivekey.ArchiveKeyResolver;
import com.zhuojl.map.reduce.common.MapReducePage;
import com.zhuojl.map.reduce.common.enums.ExecuteMode;
import com.zhuojl.map.reduce.common.exception.MyRuntimeException;
import com.zhuojl.map.reduce.reduce.Reducer;

import org.apache.logging.log4j.util.Strings;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapReduceProxy implements InvocationHandler {

    public static final String COUNT = "Count";
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
        ArchiveKey originalArchiveKey = archiveKeyResolver.extract(method, args);
        Reducer reducer = extractReducer(sharding);

        if (ExecuteMode.FIND_FIRST.equals(sharding.executeMode())) {
            return list.stream()
                    .filter(item -> filter(item, originalArchiveKey))
                    .map(item ->
                            doExecute(method, item, args)
                    )
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        if (ExecuteMode.ALL.equals(sharding.executeMode())) {
            return list.stream()
                    .filter(item -> filter(item, originalArchiveKey))
                    .map(item ->
                            doExecute(method, item, args)
                    )
                    .filter(Objects::nonNull)
                    .reduce((obj1, obj2) -> reducer.reduce(obj1, obj2))
                    .orElse(null);
        }

        // fixme 抽象策略
        if (ExecuteMode.PAGE.equals(sharding.executeMode())) {

            MapReducePage mapReducePage = getMapReducePage(args);
            Objects.requireNonNull(mapReducePage, "mapReducePage must be not null");
            if (method.getReturnType().isAssignableFrom(MapReducePage.class)) {
                throw new MyRuntimeException("page method with error return type");
            }

            List<MapReduceAble> filteredList = list.stream()
                    .filter(item -> filter(item, originalArchiveKey))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(filteredList)) {
                // XXX 如果没有则返回 reduce查询原始对象/或者clone对象，当然这样可能会有问题
                return mapReducePage;
            }

            // 便利执行 计数方法 返回 类全名，每区块计数器
            Map<MapReduceAble, Integer> countMap = getCountMap(filteredList, method, args);
            Integer count = countMap.values().stream().reduce(Integer::sum).orElse(0);
            if (count == 0) {
                // XXX 如果没有则返回 reduce查询原始对象/或者clone对象，当然这样可能会有问题
                return mapReducePage;
            }

            // 执行结果
            Map<MapReduceAble, Object> resultMap = getResultMap(filteredList, method, args, mapReducePage, countMap);

            Object obj = list.stream()
                    .filter(item -> countMap.containsKey(item) && resultMap.containsKey(item))
                    .map(item -> resultMap.get(item))
                    .reduce((obj1, obj2) -> reducer.reduce(obj1, obj2))
                    .orElse(null);

            if (Objects.isNull(obj)) {
                // XXX 如果没有则返回 reduce查询原始对象/或者clone对象，当然这样可能会有问题
                return mapReducePage;
            }

            ((MapReducePage) obj).setTotalCount(count);
            return obj;

        }


        throw new UnsupportedOperationException("sth wrong");
    }

    private MapReducePage getMapReducePage(Object[] args) {
        MapReducePage mapReducePage = null;
        for (Object obj : args) {
            if (obj instanceof MapReducePage) {
                mapReducePage = (MapReducePage) obj;
            }
        }
        return mapReducePage;
    }

    private Map<MapReduceAble, Integer> getCountMap(List<MapReduceAble> filteredList, Method method,
                                                    Object[] args) {

        Map<MapReduceAble, Integer> countMap = new HashMap<>(filteredList.size());
        for (MapReduceAble item : filteredList) {
            Method countMethod;
            try {
                countMethod = item.getClass().getMethod(method.getName() + COUNT, method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                log.error("NoSuchMethodException", e);
                throw new MyRuntimeException("NoSuchMethodException");
            }

            Object count = doExecute(countMethod, item, args);

            countMap.put(item, Objects.isNull(count) ? 0 : (Integer) count);
        }
        return countMap;

    }

    private Map<MapReduceAble, Object> getResultMap(List<MapReduceAble> filteredList,
                                                    Method method, Object[] args,
                                                    MapReducePage mapReducePage,
                                                    Map<MapReduceAble, Integer> countMap) {

        Map<MapReduceAble, Object> map = new HashMap<>();

        // adjust Page Params(start, limit)
        MapReducePageAdjuster mapReducePageAdjuster = new MapReducePageAdjuster(mapReducePage);

        for (MapReduceAble mapReduceAble : filteredList) {

            Object[] adjustParam = mapReducePageAdjuster.adjustParam(countMap.get(mapReduceAble), args);

            if (Objects.isNull(adjustParam)) {
                continue;
            }
            Object result = doExecute(method, mapReduceAble, adjustParam);
            map.put(mapReduceAble, result);
        }

        return map;
    }


    /**
     * 执行请求
     *
     * @param mapReduceAble      invoke 的 target
     * @param executedParams     原始参数
     */
    private Object doExecute(Method method, MapReduceAble mapReduceAble, Object[] executedParams) {

        try {
            return method.invoke(mapReduceAble, executedParams);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException ", e);
            throw new MyRuntimeException("c");
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException", e);
            throw new MyRuntimeException("d");
        }


    }

    private boolean filter(MapReduceAble mapReduceAble, ArchiveKey originalArchiveKey) {

        // 类配置是否和查询有交集
        return Objects.nonNull(mapReduceAble.intersectionArchiveKey(originalArchiveKey));
    }



    private Reducer extractReducer(MapReduceMethodConfig sharding) {
        String resultReducerBeanName = Strings.isBlank(sharding.reducer()) ?
                Reducer.DefaultReducer.BEAN_NAME : sharding.reducer();

        Reducer reducer = reduceMap.get(resultReducerBeanName);
        Objects.requireNonNull(reducer);
        return reducer;
    }

}