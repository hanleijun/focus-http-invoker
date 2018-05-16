package cn.focus.dc.focusaudit.common.httputil.factory;

import cn.focus.dc.focusaudit.common.httputil.binding.HttpUtilRegister;
import cn.focus.dc.focusaudit.common.httputil.delegation.HttpUtilDelegation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * Default httpUtil's factory bean
 *
 * @Author: focus eco
 * @Date: 2017-01-15
 */
public class HttpUtilFactoryBean<T> implements InitializingBean, FactoryBean<T> {

    private static final Logger logger = Logger.getLogger(HttpUtilFactoryBean.class);

    private Class<T> httpUtilInterface;

    private HttpUtilRegister httpUtilRegister;

    private HttpUtilDelegation httpUtilDelegation;

    public HttpUtilFactoryBean() {

    }

    public HttpUtilFactoryBean(Class<T> httpUtilInterface) {
        this.httpUtilInterface = httpUtilInterface;
    }


    public Class<T> getHttpUtilInterface() {
        return httpUtilInterface;
    }

    public void setHttpUtilInterface(Class<T> httpUtilInterface) {
        this.httpUtilInterface = httpUtilInterface;
    }

    public HttpUtilRegister getHttpUtilRegister() {
        return httpUtilRegister;
    }

    public void setHttpUtilRegister(HttpUtilRegister httpUtilRegister) {
        this.httpUtilRegister = httpUtilRegister;
    }

    public HttpUtilDelegation getHttpUtilDelegation() {
        return httpUtilDelegation;
    }

    public void setHttpUtilDelegation(HttpUtilDelegation httpUtilDelegation) {
        this.httpUtilDelegation = httpUtilDelegation;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.httpUtilInterface, "Property \'httpUtilInterface\' is required");
        Assert.notNull(this.httpUtilRegister, "Property \'httpUtilRegister\' is required");
        Assert.notNull(this.httpUtilDelegation, "Property \'httpUtilDelegation\' is required");
    }

    @Override
    public T getObject() throws Exception {
        return httpUtilRegister.getHttpUtil(httpUtilInterface, httpUtilDelegation);
    }

    @Override
    public Class<?> getObjectType() {
        return httpUtilInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
