package cn.focus.dc.focusaudit.common.httputil.annotation;

import cn.focus.dc.focusaudit.common.httputil.builder.DefaultHeaderBuilder;
import cn.focus.dc.focusaudit.common.httputil.builder.HeaderBuilder;
import cn.focus.dc.focusaudit.common.httputil.enums.HttpMethod;

import java.lang.annotation.*;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * 将标记了{@link HttpUtil}的接口中的抽象方法标记为http调用接口
 * <p>
 * 标记后可以直接调用该接口的此抽象方法
 * <p>
 * 该方法会访问相应接口，并返回结果
 *
 * @Author: focus eco
 * @Date: 2017-01-13
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Api {

    /**
     * 指定访问url
     * <p>
     * 若方法的参数中没有{@link Host}标记的参数,此url会直接被访问
     * <p>
     * 若方法的参数中有{@link Host}标记的参数，此url会拼接在标记有{@link Host}的参数值后方(如果有多个Host标记，取第一个)
     */
    String url();


    /**
     * 指定访问方法,默认GET方法访问
     */
    HttpMethod method() default HttpMethod.GET;

    /**
     * 指定请求中的header信息生成类
     * 默认生成类请参考{@link DefaultHeaderBuilder}
     */
    Class<? extends HeaderBuilder> headerBuilder() default DefaultHeaderBuilder.class;

}
