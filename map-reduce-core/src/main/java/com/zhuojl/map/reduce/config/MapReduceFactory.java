package com.zhuojl.map.reduce.config;

import com.zhuojl.map.reduce.MapReduceAble;
import com.zhuojl.map.reduce.ComposeParam;
import com.zhuojl.map.reduce.ComposeParamHandler;
import com.zhuojl.map.reduce.annotation.MapReduceMethodConfig;
import com.zhuojl.map.reduce.common.exception.MyRuntimeException;
import com.zhuojl.map.reduce.reduce.Reducer;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 代理工厂类
 *
 * @author zhuojl
 */
@Slf4j
public class MapReduceFactory implements FactoryBean, BeanClassLoaderAware, ApplicationContextAware {

    private Class type;

    private ApplicationContext applicationContext;

    private ClassLoader classLoader;

    @Override
    public Object getObject() {
        String[] testInterfaces = applicationContext.getBeanNamesForType(type);

        Map<String, ComposeParamHandler> map = applicationContext.getBeansOfType(ComposeParamHandler.class);
        Map<String, Reducer> reduceMap = applicationContext.getBeansOfType(Reducer.class);
        List list = Arrays.stream(testInterfaces)
                .filter(beanName -> !beanName.equals(type.getName()))
                .map(beanName -> applicationContext.getBean(beanName))
                .collect(Collectors.toList());

        MyProxy myProxy = new MyProxy(list, map, reduceMap);

        return Proxy.newProxyInstance(this.classLoader, new Class[]{type}, myProxy);
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * 反射需要用到，勿删
     */
    public Class getType() {
        return type;
    }

    /**
     * 反射需要用到，勿删
     */
    public void setType(Class type) {
        this.type = type;
    }

    private class MyProxy implements InvocationHandler {

        List list;
        Map<String, ComposeParamHandler> map;
        Map<String, Reducer> reduceMap;

        private MyProxy(List list, Map<String, ComposeParamHandler> map, Map<String, Reducer> reduceMap) {
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
