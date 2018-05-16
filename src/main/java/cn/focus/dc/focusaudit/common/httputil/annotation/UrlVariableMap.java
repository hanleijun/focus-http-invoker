package cn.focus.dc.focusaudit.common.httputil.annotation;

import java.lang.annotation.*;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * 将参数标记为 Url上的占位符(按名称)替换数据
 * <p>
 * 参数需为Map子类，否则回调过不做解析
 * <p>
 * e.g:
 * <li>url = http://www.baidu.com/{one}?two={two}</>
 * <li>params = {one:xyz,two=123}</>
 * <li> url =http://www.baidu.com/xyz?two=123 </>
 *
 * @Author: focus eco
 * @Date: 2017-01-15
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UrlVariableMap {
}
