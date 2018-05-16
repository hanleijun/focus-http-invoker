package cn.focus.dc.focusaudit.common.httputil.exception;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 *
 * @Author: focus eco
 * @Date: 2017-01-15
 */
public class BindingException extends RuntimeException {

    public BindingException() {
    }

    public BindingException(String message) {
        super(message);
    }

    public BindingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BindingException(Throwable cause) {
        super(cause);
    }
}
