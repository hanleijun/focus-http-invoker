package cn.focus.dc.focusaudit.common.httputil.enums;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 *
 * @Author: focus eco
 * @Date: 2017-01-15
 */
public enum BodyType {

    /**
     * form类型, 参数类型必须为Map子类, 会自动转换为key=value&key=value格式用x-www-form-urlencoded方式传输
     */
    FORM,
    /**
     * json类型, 参数类型必须为JSON(fastjson)的子类,会转换为json string后当json类型传输
     */
    JSON,
    /**
     * string类型, 会将参数值得toString()方法的返回值当做json传输
     */
    STRING;

    private BodyType() {
    }
}
