package cn.focus.dc.focusaudit.common.httputil.annotation;

import java.lang.annotation.*;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * <p>
 * 将参数标记为 Url上的占位符替换数据(在{@link UrlVariableMap}替换之后)
 * <p>
 * 把参数的toString方法返回值作为替换值
 * <p>
 * e.g:
 * <li>url = http://www.baidu.com/{one}</>
 * <li>params = xyz</>
 * <li> url =http://www.baidu.com/xyz </>
 *
 * @Author: focus eco
 * @Date: 2017-01-15
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UrlVariable {
}
