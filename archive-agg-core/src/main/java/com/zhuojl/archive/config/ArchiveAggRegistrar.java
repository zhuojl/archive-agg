package com.zhuojl.archive.config;

import com.zhuojl.archive.annotation.EnableAutoAgg;
import com.zhuojl.archive.annotation.ArchiveAgg;
import com.zhuojl.archive.common.exception.MyRuntimeException;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * 代理客户端注册
 *
 * @author zhuojl
 */
@Slf4j
public class ArchiveAggRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
    private ResourceLoader resourceLoader;

    private Environment environment;


    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);

        Set<String> basePackages = parsePackages(metadata);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ArchiveAgg.class));


        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner
                    .findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    // verify annotated class is an interface
                    AnnotationMetadata annotationMetadata = ((AnnotatedBeanDefinition) candidateComponent).getMetadata();
                    Assert.isTrue(annotationMetadata.isInterface(), "can only be specified on an interface");

                    BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                            .genericBeanDefinition(ArchiveAggFactory.class);
                    String className = annotationMetadata.getClassName();

                    definitionBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                    AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
                    try {
                        beanDefinition.getPropertyValues().add("type", Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        log.error("ClassNotFoundException :{}", className, e);
                        throw new MyRuntimeException("ClassNotFoundException");
                    }
                    // XXX 通过setPrimary 让应用能够方便autowire
                    beanDefinition.setPrimary(true);
                    beanDefinitionRegistry.registerBeanDefinition(className, beanDefinition);
                }
            }
        }
    }

    private Set<String> parsePackages(AnnotationMetadata metadata) {
        Map<String, Object> attributes = metadata
                .getAnnotationAttributes(EnableAutoAgg.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        for (String pkg : (String[]) attributes.get("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }

        return basePackages;
    }


    private ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                if (!beanDefinition.getMetadata().isIndependent()) {
                    return false;
                }

                return !beanDefinition.getMetadata().isAnnotation();
            }
        };
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
