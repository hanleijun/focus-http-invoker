package cn.focus.dc.focusaudit.common.httputil.builder;

import org.springframework.http.HttpHeaders;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * Interface for build own http header
 *
 * @Author: focus eco
 * @Date: 2017-01-15
 */
public interface HeaderBuilder {

    HttpHeaders build();

}
