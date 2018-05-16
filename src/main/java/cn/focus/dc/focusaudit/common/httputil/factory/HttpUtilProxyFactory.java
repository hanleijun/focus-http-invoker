package cn.focus.dc.focusaudit.common.httputil.factory;


import cn.focus.dc.focusaudit.common.httputil.delegation.HttpUtilDelegation;
import cn.focus.dc.focusaudit.common.httputil.proxy.HttpUtilProxyHandler;
import org.springframework.cglib.proxy.Proxy;


/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * Factory of httpUtil's proxy
 *
 * @Author: focus eco
 * @Date: 2017-01-15
 */
public class HttpUtilProxyFactory<T> {

    private final Class<T> httpUtilInterface;

    public HttpUtilProxyFactory(Class<T> mapperInterface) {
        this.httpUtilInterface = mapperInterface;
    }

    protected T newInstance(HttpUtilProxyHandler httpUtilProxyHandler) {
        return (T) Proxy.newProxyInstance(httpUtilInterface.getClassLoader(), new Class[]{httpUtilInterface}, httpUtilProxyHandler);
    }

    public T newInstance(HttpUtilDelegation httpUtilDelegation) {
        HttpUtilProxyHandler httpUtilProxyHandler = new HttpUtilProxyHandler(httpUtilDelegation);
        return this.newInstance(httpUtilProxyHandler);
    }

}
