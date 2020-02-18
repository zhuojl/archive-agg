package com.zhuojl.archive.config;

import com.zhuojl.archive.ArchiveAggAble;
import com.zhuojl.archive.annotation.ArchiveAggMethodConfig;
import com.zhuojl.archive.archivekey.ArchiveKey;
import com.zhuojl.archive.archivekey.ArchiveKeyResolver;
import com.zhuojl.archive.common.ArchiveAggPage;
import com.zhuojl.archive.common.enums.ExecuteMode;
import com.zhuojl.archive.common.exception.MyRuntimeException;
import com.zhuojl.archive.agg.Aggregator;

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

/**
 * 代理类
 * @author zhuojl
 */
@Slf4j
public class ArchiveAggProxy implements InvocationHandler {

    public static final String COUNT = "Count";
    private final List<ArchiveAggAble> list;
    private final Map<String, ArchiveKeyResolver> map;
    private final Map<String, Aggregator> reduceMap;

    public ArchiveAggProxy(List<ArchiveAggAble> list, Map<String, ArchiveKeyResolver> map, Map<String, Aggregator> reduceMap) {
        this.list = list;
        this.map = map;
        this.reduceMap = reduceMap;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {

        ArchiveAggMethodConfig sharding = method.getAnnotation(ArchiveAggMethodConfig.class);
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

    private Object executeByMapReduce(Method method, Object[] args, ArchiveAggMethodConfig sharding) {

        ArchiveKeyResolver archiveKeyResolver = map.get(sharding.paramHandler());
        ArchiveKey originalArchiveKey = archiveKeyResolver.extract(method, args);
        Aggregator aggregator = extractReducer(sharding);

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
                    .reduce((obj1, obj2) -> aggregator.aggregate(obj1, obj2))
                    .orElse(null);
        }

        // fixme 抽象策略
        if (ExecuteMode.PAGE.equals(sharding.executeMode())) {

            ArchiveAggPage archiveAggPage = getMapReducePage(args);
            Objects.requireNonNull(archiveAggPage, "mapReducePage must be not null");
            if (method.getReturnType().isAssignableFrom(ArchiveAggPage.class)) {
                throw new MyRuntimeException("page method with error return type");
            }

            List<ArchiveAggAble> filteredList = list.stream()
                    .filter(item -> filter(item, originalArchiveKey))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(filteredList)) {
                // XXX 如果没有则返回 reduce查询原始对象/或者clone对象，当然这样可能会有问题
                return archiveAggPage;
            }

            // 便利执行 计数方法 返回 类全名，每区块计数器
            Map<ArchiveAggAble, Integer> countMap = getCountMap(filteredList, method, args);
            Integer count = countMap.values().stream().reduce(Integer::sum).orElse(0);
            if (count == 0) {
                // XXX 如果没有则返回 reduce查询原始对象/或者clone对象，当然这样可能会有问题
                return archiveAggPage;
            }

            // 执行结果
            Map<ArchiveAggAble, Object> resultMap = getResultMap(filteredList, method, args, archiveAggPage, countMap);

            Object obj = list.stream()
                    .filter(item -> countMap.containsKey(item) && resultMap.containsKey(item))
                    .map(item -> resultMap.get(item))
                    .reduce((obj1, obj2) -> aggregator.aggregate(obj1, obj2))
                    .orElse(null);

            if (Objects.isNull(obj)) {
                // XXX 如果没有则返回 reduce查询原始对象/或者clone对象，当然这样可能会有问题
                return archiveAggPage;
            }

            ((ArchiveAggPage) obj).setTotalCount(count);
            return obj;

        }


        throw new UnsupportedOperationException("sth wrong");
    }

    private ArchiveAggPage getMapReducePage(Object[] args) {
        ArchiveAggPage archiveAggPage = null;
        for (Object obj : args) {
            if (obj instanceof ArchiveAggPage) {
                archiveAggPage = (ArchiveAggPage) obj;
            }
        }
        return archiveAggPage;
    }

    private Map<ArchiveAggAble, Integer> getCountMap(List<ArchiveAggAble> filteredList, Method method,
                                                     Object[] args) {

        Method countMethod;
        try {
            countMethod = method.getDeclaringClass().getMethod(method.getName() + COUNT, method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            log.error("NoSuchMethodException", e);
            throw new MyRuntimeException("page count method is needed for this method");
        }

        Map<ArchiveAggAble, Integer> countMap = new HashMap<>(filteredList.size());
        for (ArchiveAggAble item : filteredList) {
            Object count = doExecute(countMethod, item, args);
            countMap.put(item, Objects.isNull(count) ? 0 : (Integer) count);
        }
        return countMap;

    }

    private Map<ArchiveAggAble, Object> getResultMap(List<ArchiveAggAble> filteredList,
                                                     Method method, Object[] args,
                                                     ArchiveAggPage archiveAggPage,
                                                     Map<ArchiveAggAble, Integer> countMap) {

        Map<ArchiveAggAble, Object> resultMap = new HashMap<>();

        // adjust Page Params(start, limit)
        ArchiveAggPageAdjuster archiveAggPageAdjuster = new ArchiveAggPageAdjuster(archiveAggPage);

        for (ArchiveAggAble archiveAggAble : filteredList) {

            Object[] adjustParam = archiveAggPageAdjuster.adjustParam(countMap.get(archiveAggAble), args);

            if (Objects.isNull(adjustParam)) {
                continue;
            }
            Object result = doExecute(method, archiveAggAble, adjustParam);
            resultMap.put(archiveAggAble, result);
        }

        return resultMap;
    }


    /**
     * 执行请求
     *
     * @param archiveAggAble      invoke 的 target
     * @param executedParams     原始参数
     */
    private Object doExecute(Method method, ArchiveAggAble archiveAggAble, Object[] executedParams) {

        try {
            return method.invoke(archiveAggAble, executedParams);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException ", e);
            throw new MyRuntimeException("c");
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException", e);
            throw new MyRuntimeException("d");
        }


    }

    private boolean filter(ArchiveAggAble archiveAggAble, ArchiveKey originalArchiveKey) {

        // 类配置是否和查询有交集
        return Objects.nonNull(archiveAggAble.intersectionArchiveKey(originalArchiveKey));
    }



    private Aggregator extractReducer(ArchiveAggMethodConfig sharding) {
        String resultReducerBeanName = Strings.isBlank(sharding.aggregate()) ?
                Aggregator.DefaultAggregator.BEAN_NAME : sharding.aggregate();

        Aggregator aggregator = reduceMap.get(resultReducerBeanName);
        Objects.requireNonNull(aggregator);
        return aggregator;
    }

}