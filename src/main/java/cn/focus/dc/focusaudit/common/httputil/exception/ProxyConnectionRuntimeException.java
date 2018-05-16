package cn.focus.dc.focusaudit.common.httputil.exception;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 *
 * @Author: focus eco
 * @Date: 2017-01-17
 */
public class ProxyConnectionRuntimeException extends RuntimeException {

    public ProxyConnectionRuntimeException() {
    }

    public ProxyConnectionRuntimeException(String message) {
        super(message);
    }

    public ProxyConnectionRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyConnectionRuntimeException(Throwable cause) {
        super(cause);
    }
}
