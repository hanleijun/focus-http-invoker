package cn.focus.dc.focusaudit.common.httputil.annotation;

import java.lang.annotation.*;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * Created on 2/13/17.
 * <p>
 * 将参数标记为 Url上的参数生成参考数据
 * <p>
 * 必须为Map子类，否则不做解析
 * <p>
 * e.g:
 * <li>url = http://www.baidu.com</>
 * <li>params = {key1:value1}</>
 * <li> url =http://www.baidu.com?key1=value1 </>
 *
 * @author: focus eco
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UrlParams {
}
