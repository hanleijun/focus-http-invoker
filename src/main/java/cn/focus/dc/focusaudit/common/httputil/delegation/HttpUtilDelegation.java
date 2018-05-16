package cn.focus.dc.focusaudit.common.httputil.delegation;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;

import java.net.URI;
import java.util.concurrent.ExecutionException;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * The interface of a exactly way to call api
 *
 * @Author: focus eco
 * @Date: 2017-01-13
 */
public interface HttpUtilDelegation<T> {

    ListenableFuture<ResponseEntity<T>> get(URI url, HttpEntity entity, Class<?> responseType) throws ExecutionException, InterruptedException;

    ListenableFuture<ResponseEntity<T>> post(URI url, HttpEntity<?> entity, Class<T> responseType);

    ListenableFuture<ResponseEntity<T>> put(URI url, HttpEntity entity, Class<?> responseType);

    ListenableFuture<ResponseEntity<T>> delete(URI url, HttpEntity entity, Class<?> responseType);

    ListenableFuture<ResponseEntity<T>> options(URI url, HttpEntity entity, Class<?> responseType);

    ListenableFuture<ResponseEntity<T>> head(URI url, HttpEntity entity, Class<?> responseType);

    ListenableFuture<ResponseEntity<T>> patch(URI url, HttpEntity entity, Class<?> responseType);

    ListenableFuture<ResponseEntity<T>> trace(URI url, HttpEntity entity, Class<?> responseType);

}
