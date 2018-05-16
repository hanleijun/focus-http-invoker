package cn.focus.dc.focusaudit.common.httputil.annotation;

import cn.focus.dc.focusaudit.common.httputil.enums.BodyType;

import java.lang.annotation.*;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * 在标记有{@link Api}的抽象方法的参数中指定请求的body
 *
 * @Author: focus eco
 * @Date: 2017-01-15
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BodyContent {

    /**
     * 请求body数据的类型，默认是普通的String类型
     */
    BodyType value() default BodyType.STRING;

}
