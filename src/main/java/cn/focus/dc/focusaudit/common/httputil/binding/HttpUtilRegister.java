package cn.focus.dc.focusaudit.common.httputil.binding;

import cn.focus.dc.focusaudit.common.httputil.delegation.HttpUtilDelegation;
import cn.focus.dc.focusaudit.common.httputil.exception.BindingException;
import cn.focus.dc.focusaudit.common.httputil.factory.HttpUtilProxyFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * HttpUtil interface and proxy register here
 *
 * @Author: focus eco
 * @Date: 2017-01-15
 */
public class HttpUtilRegister {

    private final Map<Class<?>, HttpUtilProxyFactory<?>> knownUtils = new HashMap<>();

    public <T> T getHttpUtil(Class<T> type, HttpUtilDelegation httpUtilDelegation) {
        HttpUtilProxyFactory httpUtilProxyFactory = knownUtils.get(type);
        if (httpUtilProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the HttpUtilRegister.");
        } else {
            try {
                return (T) httpUtilProxyFactory.newInstance(httpUtilDelegation);
            } catch (Exception e) {
                throw new BindingException("Error getting httpUtil instance. Cause: " + e, e);
            }
        }
    }

    private <T> boolean hasHttpUtil(Class<T> type) {
        return knownUtils.containsKey(type);
    }

    <T> void addHttpUtil(Class<T> type) {
        if (type.isInterface()) {
            if (this.hasHttpUtil(type)) {
                throw new BindingException("Type " + type + " is already known to the HttpUtilRegister.");
            }

            boolean loadCompleted = false;

            try {
                knownUtils.put(type, new HttpUtilProxyFactory(type));
                loadCompleted = true;
            } finally {
                if (!loadCompleted) {
                    this.knownUtils.remove(type);
                }

            }
        }

    }

    public Collection<Class<?>> getHttpUtils() {
        return Collections.unmodifiableCollection(this.knownUtils.keySet());
    }

}
