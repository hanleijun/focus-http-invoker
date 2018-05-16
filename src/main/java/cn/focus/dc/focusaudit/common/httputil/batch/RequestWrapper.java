package cn.focus.dc.focusaudit.common.httputil.batch;

import cn.focus.dc.focusaudit.common.httputil.exception.ParsingException;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Copyright (C) 1998 - 2018 SOHU Inc., All Rights Reserved.
 * <p>
 *
 * @author: leijunhan (leijunhan@sohu-inc.com)
 * @date: 2018/2/7
 */
public class RequestWrapper<K, V> {
    private String host;
    private String uri;
    private Map<K, V> paramMap;
    private Query query;

    /**
     * the query part of a request
     */
    public class Query {
        private String uri;
        private String q;

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getQ() {
            return q;
        }

        public void setQ(String q) {
            this.q = q;
        }

        Query parse(String uri, Map<K, V> paramMap){
            if(StringUtils.isBlank(uri)){
                throw new ParsingException("uri is a required parameter");
            }
            if(null != paramMap){
                List qList = Lists.newArrayList();
                paramMap.forEach((x, y) -> qList.add(x + "=" + y)
                );
                this.q = StringUtils.join(qList, "&");
            }else{
                this.q = StringUtils.EMPTY;
            }
            this.uri = uri;
            return this;
        }
    }
    /**
     * 用于批量请求的封装单元
     * @param host 请求的域名, eg: http://house-sv-base.focus.cn
     * @param uri 请求的URI, eg: city/list
     * @param paramMap 请求参数的{@link Map<K, V>}, 如果没有参数则传{@code null}
     */
    public RequestWrapper(String host, String uri, Map<K, V> paramMap) {
        this.host = host;
        this.uri = uri;
        this.paramMap = paramMap;
        this.query = new Query();
        this.query = this.query.parse(uri, paramMap);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Map<K, V> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<K, V> paramMap) {
        this.paramMap = paramMap;
    }
}
