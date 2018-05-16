package cn.focus.dc.focusaudit.common.httputil.delegation;

import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SuccessCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Copyright (C) 1998 - 2017 SOHU Inc., All Rights Reserved.
 * <p>
 *
 * @author: leijunhan (leijunhan@sohu-inc.com)
 * @date: 2018/1/29
 */
public class FutureWrapper<T> implements ListenableFuture<T> {
    private T responseEntity;

    public T getResponseEntity() {
        return responseEntity;
    }

    public void setResponseEntity(T responseEntity) {
        this.responseEntity = responseEntity;
    }

    public FutureWrapper(T responseEntity) {
        this.responseEntity = responseEntity;
    }

    @Override
    public void addCallback(ListenableFutureCallback<? super T> listenableFutureCallback) {

    }

    @Override
    public void addCallback(SuccessCallback<? super T> successCallback, FailureCallback failureCallback) {

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return responseEntity;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return responseEntity;
    }
}
