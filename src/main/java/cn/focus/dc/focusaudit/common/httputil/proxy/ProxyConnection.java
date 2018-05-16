package cn.focus.dc.focusaudit.common.httputil.proxy;

import cn.focus.dc.focusaudit.common.httputil.annotation.*;
import cn.focus.dc.focusaudit.common.httputil.builder.HeaderBuilder;
import cn.focus.dc.focusaudit.common.httputil.delegation.HttpUtilDelegation;
import cn.focus.dc.focusaudit.common.httputil.enums.BodyType;
import cn.focus.dc.focusaudit.common.httputil.enums.HttpMethod;
import cn.focus.dc.focusaudit.common.httputil.exception.ProxyConnectionRuntimeException;
import cn.focus.dc.focusaudit.common.httputil.props.Props;
import com.alibaba.fastjson.JSON;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * Assemble the url for connection
 * <p>
 * Get the result of calling url
 *
 * @author: focus eco
 * @Date: 2017-01-17
 */

@Component
@EnableConfigurationProperties(Props.class)
public class ProxyConnection implements EnvironmentAware {

    private static final Logger logger = Logger.getLogger(ProxyConnection.class);

    private String url;

    private HttpHeaders httpHeaders;

    private Class<?> responseType;

    private Class<?> responseGenericReturnType;

    private HttpMethod httpMethod;

    private HttpEntity httpEntity;

    private List<Param> urlVParams;

    private List<Param> urlVMParams;

    private List<Param> urlParams;

    private List<Param> bodyCParams;

    private List<Param> hostParams;

    @Autowired
    private Props props;
    @Autowired
    private RestTemplate restTemplate;

    private static String appId;

    public ProxyConnection() {

    }

    @Override
    public void setEnvironment(Environment environment) {
        appId = props.getAppId();
        System.out.println(appId);
    }

    private class Param {

        private Parameter parameter;

        private Object value;

        public Param(Parameter parameter, Object value) {
            this.parameter = parameter;
            this.value = value;
        }

        public Parameter getParameter() {
            return parameter;
        }

        public void setParameter(Parameter parameter) {
            this.parameter = parameter;
        }

        public Object getValue() {
            return value == null ? "null" : value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    public ProxyConnection(Object object, Method method, Object[] objects) {
        init(object, method, objects);
    }

    public void parse() {
        try {
            //process annotation of UrlVariableMap
            urlVMParams.stream().filter(param -> param.getValue() instanceof Map).forEach(param -> {
                Map map = (Map) param.getValue();
                for (Object k : map.keySet()) {
                    Object v = map.get(k);
                    try {
                        url = url.replaceAll("\\{" + k.toString() + "\\}", URLEncoder.encode(v.toString(), "utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        url = url.replaceAll("\\{" + k.toString() + "\\}", v.toString());
                    }
                }
            });

            //process annotation of UrlVariable
            urlVParams.forEach(param -> {
                try {
                    url = url.replaceFirst("\\{[^{,^}]*\\}", URLEncoder.encode(param.getValue().toString(), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    url = url.replaceFirst("\\{[^{,^}]*\\}", param.getValue().toString());
                }
            });

            //process annotation of UrlParams
            if (urlParams.size() > 0) {
                //ignore the param which has the index larger than 0
                Param param = urlParams.get(0);
                if (param.getValue() instanceof Map) {
                    Map map = (Map) param.getValue();
                    TreeMap treeMap = Maps.newTreeMap((x, y) -> x.toString().compareToIgnoreCase(y.toString()));
                    map.forEach((k, v) -> treeMap.put(k, v));
                    for (Object k : treeMap.keySet()) {
                        Object v = treeMap.get(k);
                        if (v == null) {
                            continue;
                        }
                        String value;
                        try {
                            value = URLEncoder.encode(v.toString(), "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            value = v.toString();
                        }
                        if (!url.contains("?")) {
                            url = String.format("%s?%s=%s", url, k.toString(), value);
                        } else {
                            url = String.format("%s&%s=%s", url, k.toString(), value);
                        }
                    }
                }
            }

            //process annotation of Host
            if (hostParams.size() > 0) {
                //ignore the param which has the index larger than 0
                Param param = hostParams.get(0);
                url = param.getValue().toString() + url;
            }

            //process annotation of BodyContent
            if (bodyCParams.size() > 0) {
                //ignore the param which has the index larger than 0
                Param param = bodyCParams.get(0);
                BodyContent bodyContent = param.getParameter().getAnnotation(BodyContent.class);
                BodyType bodyType = bodyContent.value();
                switch (bodyType) {
                    case STRING:
                        if (httpHeaders.getContentType() == null) {
                            httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
                        }
                        httpEntity = new HttpEntity(param.getValue().toString(), httpHeaders);
                        break;
                    case JSON:
                        if (!(param.getValue() instanceof JSON)) break;
                        if (httpHeaders.getContentType() == null) {
                            httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
                        }
                        httpEntity = new HttpEntity(param.getValue().toString(), httpHeaders);
                        break;
                    case FORM:
                        if (httpHeaders.getContentType() == null) {
                            httpHeaders.setContentType(MediaType.parseMediaType("application/x-www-form-urlencoded;charset=UTF-8"));
                        }
                        if (param.getValue() instanceof Map) {
                            StringBuilder sb = new StringBuilder();
                            Map map = (Map) param.getValue();
                            for (Object k : map.keySet()) {
                                Object v = map.get(k);
                                if (v == null) {
                                    continue;
                                }
                                String key = URLEncoder.encode(k.toString(), "utf-8");
                                sb.append(key);
                                sb.append("=");
                                String value = URLEncoder.encode(v.toString(), "utf-8");
                                sb.append(value);
                                sb.append("&");
                            }
                            if (sb.length() > 0) {
                                sb.deleteCharAt(sb.length() - 1);
                            }

                            httpEntity = new HttpEntity(sb.toString(), httpHeaders);
                        } else if (param.getParameter().getType().isAssignableFrom(String.class)) {
                            String content = (String) param.getValue();
                            httpEntity = new HttpEntity(content, httpHeaders);
                        }
                        break;
                }
            }
            if(url.contains("?")){
                url = url + "&appId=" + appId;
            }else{
                url = url + "?appId=" + appId;
            }
        } catch (Exception e) {
            logger.error("proxy connection parse failed", e);
            throw new ProxyConnectionRuntimeException("proxy connection parse failed");
        }
    }

    public Object proxy(HttpUtilDelegation httpUtilDelegation) {
        try {
            if (httpEntity == null) {
                httpEntity = new HttpEntity(httpHeaders);
            }
            ListenableFuture<ResponseEntity> responseEntity;
            Class<?> type = responseGenericReturnType == null ? responseType : responseGenericReturnType;
            URI uri = new URI(url);
            long startTime = System.currentTimeMillis();
            switch (httpMethod) {
                case GET:
                    responseEntity = httpUtilDelegation.get(uri, httpEntity, type);
                    break;
                case POST:
                    responseEntity = httpUtilDelegation.post(uri, httpEntity, type);
                    break;
                case DELETE:
                    responseEntity = httpUtilDelegation.delete(uri, httpEntity, type);
                    break;
                case PUT:
                    responseEntity = httpUtilDelegation.put(uri, httpEntity, type);
                    break;
                case OPTIONS:
                    responseEntity = httpUtilDelegation.options(uri, httpEntity, type);
                    break;
                case HEAD:
                    responseEntity = httpUtilDelegation.head(uri, httpEntity, type);
                    break;
                case PATCH:
                    responseEntity = httpUtilDelegation.patch(uri, httpEntity, type);
                    break;
                case TRACE:
                    responseEntity = httpUtilDelegation.trace(uri, httpEntity, type);
                    break;
                default:
                    throw new ProxyConnectionRuntimeException("unknown http method [" + httpMethod + "]");
            }
            long endTime = System.currentTimeMillis();
            logger.info("Third part url:" + uri +", cost time: " + (endTime - startTime) + "ms");
            logger.info(String.format("Proxy connection info: url: %s, requestEntity: %s, responseEntity: %s", uri, httpEntity, responseEntity));
            if (responseType.isAssignableFrom(ListenableFuture.class)) {
                return responseEntity;
            }
            if (responseEntity != null) {
                return responseEntity;
            }
            return null;
        } catch (HttpServerErrorException he) {
            logger.error(
                    String.format("proxy connection request error. url: %s, requestEntity: %s, httpCode: %d, desc: %s, body: %s",
                            url,
                            httpEntity,
                            he.getStatusCode().value(),
                            he.getStatusText(),
                            he.getResponseBodyAsString()));
            throw new ProxyConnectionRuntimeException("proxy connection request error.");
        } catch (Exception e) {
            logger.error(String.format("proxy connection parse failed, url: %s, requestEntity: %s", url, httpEntity), e);
            throw new ProxyConnectionRuntimeException("proxy connection parse failed");
        }
    }

    private void init(Object object, Method method, Object[] objects) {
        try {
            Api apiConfig = method.getAnnotation(Api.class);
            httpMethod = apiConfig.method();
            url = apiConfig.url();
            Class<? extends HeaderBuilder> clazz = apiConfig.headerBuilder();
            HeaderBuilder builder = clazz.newInstance();
            httpHeaders = builder.build();

            responseType = method.getReturnType();
            responseGenericReturnType = String.class;
            urlParams = new ArrayList<>();
            urlVMParams = new ArrayList<>();
            urlVParams = new ArrayList<>();
            bodyCParams = new ArrayList<>();
            hostParams = new ArrayList<>();

            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                UrlVariable urlVariable = parameter.getAnnotation(UrlVariable.class);
                if (urlVariable != null) {
                    urlVParams.add(new Param(parameter, objects[i]));
                    continue;
                }
                UrlVariableMap urlVariableMap = parameter.getAnnotation(UrlVariableMap.class);
                if (urlVariableMap != null) {
                    urlVMParams.add(new Param(parameter, objects[i]));
                    continue;
                }
                UrlParams urlParam = parameter.getAnnotation(UrlParams.class);
                if (urlParam != null) {
                    urlParams.add(new Param(parameter, objects[i]));
                    continue;
                }
                BodyContent bodyContent = parameter.getAnnotation(BodyContent.class);
                if (bodyContent != null) {
                    bodyCParams.add(new Param(parameter, objects[i]));
                    continue;
                }
                Host host = parameter.getAnnotation(Host.class);
                if (host != null) {
                    hostParams.add(new Param(parameter, objects[i]));
                    continue;
                }
                if (parameter.getType().isAssignableFrom(HttpHeaders.class)) {
                    httpHeaders = (HttpHeaders) objects[i];
                }
            }
        } catch (Exception e) {
            logger.error("proxy connection parse failed\n" + method + "\n" + object + "\n" + objects, e);
            throw new ProxyConnectionRuntimeException("proxy connection init failed");
        }
    }
}
