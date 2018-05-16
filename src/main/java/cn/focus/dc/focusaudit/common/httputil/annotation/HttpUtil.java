package cn.focus.dc.focusaudit.common.httputil.annotation;


import java.lang.annotation.*;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * 将接口标记为HttpUtil接口
 * <p>
 * 将会生成该接口动态代理，不用实现此接口即可调用
 *
 * @Author: focus eco
 * @Date: 2017-01-15
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpUtil {
}
