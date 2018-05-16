package cn.focus.dc.focusaudit.common.httputil.exception;

/**
 * Copyright (C) 1998 - 2018 SOHU Inc., All Rights Reserved.
 * <p>
 * parsing data exception
 * @author: leijunhan (leijunhan@sohu-inc.com)
 * @date: 2018/2/5
 */
public class ParsingException extends RuntimeException {

    public ParsingException() {
    }

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingException(Throwable cause) {
        super(cause);
    }
}
