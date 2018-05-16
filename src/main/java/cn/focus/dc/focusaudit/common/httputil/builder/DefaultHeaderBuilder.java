package cn.focus.dc.focusaudit.common.httputil.builder;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 *
 * @Author: focus eco
 * @Date: 2017-01-15
 */
public class DefaultHeaderBuilder implements HeaderBuilder {

    @Override
    public HttpHeaders build() {
        HttpHeaders httpHeaders = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        return httpHeaders;
    }
}
