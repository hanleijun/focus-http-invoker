package cn.focus.dc.focusaudit.common.httputil.binding;

import cn.focus.dc.focusaudit.common.httputil.factory.HttpUtilFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * The realization detail of binding
 *
 * @Author: focus eco
 * @Date: 2017-01-16
 */
public class ClassPathHttpUtilScanner extends ClassPathBeanDefinitionScanner {

    private Class<? extends Annotation> annotationClass;

    private Class<?> markerInterface;

    private HttpUtilRegister httpUtilRegister;

    private HttpUtilFactoryBean<?> httpUtilFactoryBean = new HttpUtilFactoryBean();

    public ClassPathHttpUtilScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setHttpUtilRegister(HttpUtilRegister httpUtilRegister) {
        this.httpUtilRegister = httpUtilRegister;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }


    public void registerFilters() {
        boolean acceptAllInterfaces = true;
        if (this.annotationClass != null) {
            this.addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
            acceptAllInterfaces = false;
        }

        if (this.markerInterface != null) {
            this.addIncludeFilter(new AssignableTypeFilter(this.markerInterface) {
                @Override
                protected boolean matchClassName(String className) {
                    return false;
                }
            });
            acceptAllInterfaces = false;
        }

        if (acceptAllInterfaces) {
            this.addIncludeFilter((metadataReader, metadataReaderFactory2) -> true);
        }

        this.addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            String className = metadataReader.getClassMetadata().getClassName();
            return className.endsWith("package-info");
        });
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        if (beanDefinitions.isEmpty()) {
            this.logger.warn("No HttpUtils was found in \'" + Arrays.toString(basePackages) + "\' package. Please check your configuration.");
        } else {
            this.processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    private static ClassLoader getTCL() throws IllegalAccessException, InvocationTargetException {

        Method method = null;
        try {
            method = Thread.class.getMethod("getContextClassLoader", null);
        } catch (NoSuchMethodException e) {
            // We are running on JDK 1.1
            return null;
        }

        return (ClassLoader) method.invoke(Thread.currentThread(), null);
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        for (BeanDefinitionHolder holder : beanDefinitions) {
            GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();

            ClassLoader classLoader = null;
            try {
                classLoader = getTCL();
            } catch (Exception e) {
                throw new IllegalStateException("get classLoader failed");
            }
            try {
                httpUtilRegister.addHttpUtil(definition.resolveBeanClass(classLoader));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Bean class name [" + definition.getBeanClassName() + "] can not be found");
            }

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Creating HttpUtilFactoryBean with name \'" + holder.getBeanName() + "\' and \'" + definition.getBeanClassName() + "\' httpUtilInterface");
            }

            definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName());
            definition.setBeanClass(this.httpUtilFactoryBean.getClass());
            definition.getPropertyValues().add("httpUtilRegister", this.httpUtilRegister);

            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        }
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) {
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            this.logger.warn("Skipping HttpUtilFactoryBean with name \'" + beanName + "\' and \'" + beanDefinition.getBeanClassName() + "\' httpUtilInterface. Bean already defined with the same name!");
            return false;
        }
    }


}
