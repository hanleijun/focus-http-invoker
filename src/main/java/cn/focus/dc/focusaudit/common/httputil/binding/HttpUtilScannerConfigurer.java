package cn.focus.dc.focusaudit.common.httputil.binding;

import cn.focus.dc.focusaudit.common.httputil.delegation.HttpUtilDelegation;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * Scan the interfaces of httpUtil that need to be proxy
 *
 * @Author: focus eco
 * @Date: 2017-01-16
 */
public class HttpUtilScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {

    private boolean processPropertyPlaceHolders;

    private String basePackage;

    private HttpUtilDelegation httpUtilDelegation;

    private Class<? extends Annotation> annotationClass;

    private Class<?> markerInterface;

    private String beanName;

    private ApplicationContext applicationContext;

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public void setHttpUtilDelegation(HttpUtilDelegation httpUtilDelegation) {
        this.httpUtilDelegation = httpUtilDelegation;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    public void setProcessPropertyPlaceHolders(boolean processPropertyPlaceHolders) {
        this.processPropertyPlaceHolders = processPropertyPlaceHolders;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.basePackage, "Property \'basePackage\' is required");
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        if (this.processPropertyPlaceHolders) {
            processPropertyPlaceHolders();
        }

        ClassPathHttpUtilScanner scanner = new ClassPathHttpUtilScanner(beanDefinitionRegistry);
        scanner.setAnnotationClass(this.annotationClass);
        scanner.setMarkerInterface(this.markerInterface);
        scanner.setResourceLoader(this.applicationContext);

        HttpUtilRegister httpUtilRegister = new HttpUtilRegister();
        scanner.setHttpUtilRegister(httpUtilRegister);

        scanner.registerFilters();
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ",; \t\n"));

    }

    private void processPropertyPlaceHolders() {
        Map prcs = this.applicationContext.getBeansOfType(PropertyResourceConfigurer.class);
        if (!prcs.isEmpty() && this.applicationContext instanceof ConfigurableApplicationContext) {
            BeanDefinition mapperScannerBean = ((ConfigurableApplicationContext) this.applicationContext).getBeanFactory().getBeanDefinition(this.beanName);
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            factory.registerBeanDefinition(this.beanName, mapperScannerBean);

            for (Object o : prcs.values()) {
                PropertyResourceConfigurer prc = (PropertyResourceConfigurer) o;
                prc.postProcessBeanFactory(factory);
            }

            MutablePropertyValues values1 = mapperScannerBean.getPropertyValues();
            this.basePackage = this.updatePropertyValue("basePackage", values1);
        }

    }

    private String updatePropertyValue(String propertyName, PropertyValues values) {
        PropertyValue property = values.getPropertyValue(propertyName);
        if (property == null) {
            return null;
        } else {
            Object value = property.getValue();
            return value == null ? null : (value instanceof String ? value.toString() : (value instanceof TypedStringValue ? ((TypedStringValue) value).getValue() : null));
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
