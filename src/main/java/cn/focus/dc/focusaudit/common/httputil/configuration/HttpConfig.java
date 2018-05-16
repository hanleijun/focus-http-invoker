package cn.focus.dc.focusaudit.common.httputil.configuration;

import cn.focus.dc.focusaudit.common.httputil.annotation.HttpUtil;
import cn.focus.dc.focusaudit.common.httputil.binding.HttpUtilScannerConfigurer;
import cn.focus.dc.focusaudit.common.httputil.converter.FormHttpJSONMessageConverter;
import cn.focus.dc.focusaudit.common.httputil.delegation.HttpUtilDelegation;
import cn.focus.dc.focusaudit.common.httputil.delegation.RestTemplateDelegation;
import cn.focus.eco.data.curator.core.NewConfigBean;
import cn.focus.eco.data.curator.core.RedisServiceProxy;
import cn.focus.eco.data.curator.core.SpringUtil;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 1998 - 2017 SOHU Inc., All Rights Reserved.
 * <p>
 *
 * @author: leijunhan (leijunhan@sohu-inc.com)
 * @date: 2018/1/16
 */

@Configuration
@Import({NewConfigBean.class,RedisServiceProxy.class, SpringUtil.class})
public class HttpConfig {
    @NotEmpty
    private String basePackage = "cn.focus";

    @Bean
    public ClientHttpRequestFactory requestFactory() throws Exception {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setReadTimeout(20000);
        requestFactory.setConnectTimeout(10000);
        return requestFactory;
    }

    @Autowired
    @Bean
    public RestTemplate normalRestTemplate(ClientHttpRequestFactory factory) throws Exception {
        RestTemplate restTemplate = new RestTemplate(factory);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new StringHttpMessageConverter());
        converters.add(new FormHttpJSONMessageConverter());
        converters.add(new ByteArrayHttpMessageConverter());
        restTemplate.setMessageConverters(converters);
        return restTemplate;
    }

    @Bean
    public AsyncRestTemplate asyncRestTemplate() throws Exception {
        AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new StringHttpMessageConverter());
        converters.add(new FormHttpJSONMessageConverter());
        converters.add(new ByteArrayHttpMessageConverter());
        asyncRestTemplate.setMessageConverters(converters);
        return asyncRestTemplate;
    }

    @Bean
    @Autowired
    public HttpUtilScannerConfigurer httpUtilScannerConfigurer() throws Exception {
        HttpUtilScannerConfigurer httpUtilScannerConfigurer = new HttpUtilScannerConfigurer();
        httpUtilScannerConfigurer.setBasePackage(basePackage);
        httpUtilScannerConfigurer.setAnnotationClass(HttpUtil.class);
        return httpUtilScannerConfigurer;
    }

    @Bean
    @Autowired
    public HttpUtilDelegation httpUtilDelegation(AsyncRestTemplate restTemplate, RedisServiceProxy redisServiceProxy) {
        return new RestTemplateDelegation(restTemplate, redisServiceProxy);
    }
}
