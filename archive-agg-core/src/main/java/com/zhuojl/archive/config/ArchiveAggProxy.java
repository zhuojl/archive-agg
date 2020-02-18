package com.zhuojl.archive.config;

import com.zhuojl.archive.ArchiveAggAble;
import com.zhuojl.archive.agg.Aggregator;
import com.zhuojl.archive.agg.DefaultAggregator;
import com.zhuojl.archive.annotation.ArchiveAggMethodConfig;
import com.zhuojl.archive.archivekey.ArchiveKey;
import com.zhuojl.archive.archivekey.ArchiveKeyResolver;
import com.zhuojl.archive.common.exception.MyRuntimeException;
import com.zhuojl.archive.executor.ArchiveExecutor;
import com.zhuojl.archive.executor.ArchiveExecutorContext;
import com.zhuojl.archive.executor.ArchiveExecutorFactory;

import org.apache.logging.log4j.util.Strings;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 代理类
 *
 * @author zhuojl
 */
@Slf4j
public class ArchiveAggProxy implements InvocationHandler {

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

        List<ArchiveAggAble> filteredList = list.stream()
                .filter(item -> filter(item, originalArchiveKey))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(filteredList)) {
            // 没有区间匹配的时候，就返回null，客户端需要处理NPE
            return null;
        }

        Aggregator aggregator = extractReducer(sharding);

        ArchiveExecutorContext context = ArchiveExecutorContext.builder()
                .filteredList(filteredList)
                .method(method)
                .args(args)
                .aggregator(aggregator)
                .build();


        ArchiveExecutor executor = ArchiveExecutorFactory.getExecutorFactory(sharding.executeMode(), context);
        return executor.execute();
    }


    private boolean filter(ArchiveAggAble archiveAggAble, ArchiveKey originalArchiveKey) {

        // 类配置是否和查询有交集
        return Objects.nonNull(archiveAggAble.intersectionArchiveKey(originalArchiveKey));
    }


    private Aggregator extractReducer(ArchiveAggMethodConfig sharding) {
        String resultReducerBeanName = Strings.isBlank(sharding.aggregate()) ?
                DefaultAggregator.BEAN_NAME : sharding.aggregate();

        Aggregator aggregator = reduceMap.get(resultReducerBeanName);
        Objects.requireNonNull(aggregator);
        return aggregator;
    }

}