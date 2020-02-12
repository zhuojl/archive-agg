package com.zhuojl.map.reduce.config;

import com.zhuojl.map.reduce.ComposeParam;
import com.zhuojl.map.reduce.ComposeParamHandler;
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

    private final List list;
    private final Map<String, ComposeParamHandler> map;
    private final Map<String, Reducer> reduceMap;

    public MapReduceProxy(List list, Map<String, ComposeParamHandler> map, Map<String, Reducer> reduceMap) {
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
        try {

            ComposeParamHandler composeParamHandler = map.get(sharding.paramHandler());
            // 参数拆取
            ComposeParam originalComposeParam = composeParamHandler.extract(args);

            String resultReducerBeanName = Strings.isBlank(sharding.reduceBeanName())?
                    Reducer.DefaultReducer.BEAN_NAME: sharding.reduceBeanName();

            Reducer reducer = reduceMap.get(resultReducerBeanName);
            Objects.requireNonNull(reducer);

            Object result = list.stream()
                    .filter(item -> {
                        // 多次执行会导致参数被变更，用原始参数重置参数
                        composeParamHandler.rebuild(originalComposeParam);
                        ComposeParam composeParam = composeParamHandler.extract(args);
                        if (Objects.isNull(composeParam)) {
                            return false;
                        }
                        return !Objects.isNull(((MapReduceAble) item).getExecuteParam(composeParam));
                    })
                    .map(item -> {
                        // XXX 如果需要并发执行，需要clone，再根据配置的线程池或者默认线程池进行异步处理
                        ComposeParam composeParam = composeParamHandler.extract(args);
                        ComposeParam executeParam = ((MapReduceAble) item).getExecuteParam(composeParam);
                        Object[] params = (Object[]) composeParamHandler.rebuild(executeParam);
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
                    .reduce((obj1, obj2) ->  reducer.reduce(obj1, obj2))
                    .orElse(null);

            // 参数重置
            composeParamHandler.rebuild(originalComposeParam);
            return result;
        } catch (Exception e) {
            log.error("sth error", e);
            throw new MyRuntimeException("f");
        }
    }

}