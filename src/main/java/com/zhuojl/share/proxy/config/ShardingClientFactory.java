package com.zhuojl.share.proxy.config;

import com.zhuojl.share.proxy.annotation.Sharding;
import com.zhuojl.share.proxy.common.exception.MyRuntimeException;
import com.zhuojl.share.proxy.service.OrderService;
import com.zhuojl.share.proxy.service.ShardingAble;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 代理工厂类
 *
 * @author zhuojl
 */
@Slf4j
public class ShardingClientFactory implements FactoryBean, BeanClassLoaderAware, ApplicationContextAware {

    private Class type;

    private ApplicationContext applicationContext;

    private ClassLoader classLoader;

    @Override
    public OrderService getObject() {
        String[] testInterfaces = applicationContext.getBeanNamesForType(type);


        MyProxy myProxy = new MyProxy(Arrays.stream(testInterfaces)
                .filter(beanName -> !beanName.equals(type.getName()))
                .map(beanName -> (OrderService) applicationContext.getBean(beanName))
                .collect(Collectors.toList()));

        return (OrderService) Proxy.newProxyInstance(this.classLoader, new Class[]{type}, myProxy);
    }

    @Override
    public Class<?> getObjectType() {
        return OrderService.class;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    private class MyProxy implements InvocationHandler {

        List list;

        private MyProxy(List<OrderService> list) {
            this.list = list;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {

            Sharding sharding = method.getAnnotation(Sharding.class);
            if (Objects.isNull(sharding)) {
                try {
                    return method.invoke(proxy, args);
                } catch (IllegalAccessException e) {
                    throw new MyRuntimeException("a");
                } catch (InvocationTargetException e) {
                    throw new MyRuntimeException("b");
                }
            }
            // 这里借鉴mybatis的取参数设置参数，获取到开始时间结束时间，这里省略了
            Date startTime = new Date();
            Date endTime = new Date();

            log.info("invoke class: {}, method: {}", proxy.getClass().getSimpleName(), method.getName());

            try {
                return list.stream()
                        .filter(item ->
                                Objects.nonNull(((ShardingAble) item).getTimeRange(startTime, endTime)))
                        .map(item -> {
                            // 通过mybatis设置值的方式设置值
                            try {
                                return method.invoke(item, args);
                            } catch (IllegalAccessException e) {
                                log.error("IllegalAccessException ", e);
                                throw new MyRuntimeException("c");
                            } catch (InvocationTargetException e) {
                                log.error("InvocationTargetException", e);
                                throw new MyRuntimeException("d");
                            }
                        })
                        .filter(Objects::nonNull)
                        .reduce(sharding.binaryOperator().getListBinaryOperator())
                        .orElse(null);
            } catch (Exception e) {
                log.error("", e);
                throw new MyRuntimeException("f");
            }
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
