package cn.focus.dc.focusaudit.common.httputil.annotation;

import java.lang.annotation.*;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * 将参数指定为host地址, 去参数的toString方法返回值作为host地址
 *
 * @Author: focus eco
 * @Date: 2017-01-17
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Host {

}
