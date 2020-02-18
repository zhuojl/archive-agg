package com.zhuojl.archive.config;

import com.zhuojl.archive.ArchiveAggAble;
import com.zhuojl.archive.archivekey.ArchiveKeyResolver;
import com.zhuojl.archive.agg.Aggregator;

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
public class ArchiveAggFactory implements FactoryBean, BeanClassLoaderAware, ApplicationContextAware {

    private Class type;

    private ApplicationContext applicationContext;

    private ClassLoader classLoader;

    @Override
    public Object getObject() {
        String[] testInterfaces = applicationContext.getBeanNamesForType(type);

        Map<String, ArchiveKeyResolver> map = applicationContext.getBeansOfType(ArchiveKeyResolver.class);
        Map<String, Aggregator> reduceMap = applicationContext.getBeansOfType(Aggregator.class);
        List<ArchiveAggAble> list = Arrays.stream(testInterfaces)
                // 因为代理bean使用的就是这个名字（在register时指定），需要排除自己引用自己。
                .filter(beanName -> !beanName.equals(type.getName()))
                .map(beanName -> {
                    Object bean = applicationContext.getBean(beanName);
                    return (ArchiveAggAble)bean;
                })
                // 排序主要用于确认优先级
                .sorted((o1, o2) -> o2.getArchiveKey().getOrder() - o1.getArchiveKey().getOrder())
                .collect(Collectors.toList());

        ArchiveAggProxy archiveAggProxy = new ArchiveAggProxy(list, map, reduceMap);

        return Proxy.newProxyInstance(this.classLoader, new Class[]{type}, archiveAggProxy);
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
