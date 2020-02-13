package com.zhuojl.map.reduce.config;

import com.zhuojl.map.reduce.ArchiveKeyResolver;
import com.zhuojl.map.reduce.reduce.Reducer;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

        Map<String, ArchiveKeyResolver> map = applicationContext.getBeansOfType(ArchiveKeyResolver.class);
        Map<String, Reducer> reduceMap = applicationContext.getBeansOfType(Reducer.class);
        List list = Arrays.stream(testInterfaces)
                .filter(beanName -> !beanName.equals(type.getName()))
                .map(beanName -> applicationContext.getBean(beanName))
                .collect(Collectors.toList());

        MapReduceProxy mapReduceProxy = new MapReduceProxy(list, map, reduceMap);

        return Proxy.newProxyInstance(this.classLoader, new Class[]{type}, mapReduceProxy);
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
