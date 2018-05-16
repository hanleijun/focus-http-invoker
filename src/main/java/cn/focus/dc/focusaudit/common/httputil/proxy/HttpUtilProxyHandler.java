package cn.focus.dc.focusaudit.common.httputil.proxy;

import cn.focus.dc.focusaudit.common.httputil.delegation.HttpUtilDelegation;
import org.springframework.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * Proxy for all httpUtil interface
 *
 * @Author: focus eco
 * @Date: 2017-01-13
 */
public class HttpUtilProxyHandler implements InvocationHandler {


    private HttpUtilDelegation delegation;

    public HttpUtilProxyHandler(HttpUtilDelegation delegation) {
        this.delegation = delegation;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        ProxyConnection proxyConnection = new ProxyConnection(o, method, objects);
        proxyConnection.parse();
        return proxyConnection.proxy(delegation);
    }

}
