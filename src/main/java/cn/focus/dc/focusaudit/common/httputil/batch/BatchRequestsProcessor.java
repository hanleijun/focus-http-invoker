package cn.focus.dc.focusaudit.common.httputil.batch;

import cn.focus.dc.focusaudit.common.httputil.delegation.RestTemplateDelegation;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Copyright (C) 1998 - 2018 SOHU Inc., All Rights Reserved.
 * <p>
 * processor to resolve batch requests
 * @author: leijunhan (leijunhan@sohu-inc.com)
 * @date: 2018/2/5
 */
@Component
public class BatchRequestsProcessor {
    private final static Logger logger = Logger.getLogger(BatchRequestsProcessor.class);

    private static RestTemplateDelegation httpUtilDelegation;

    @Autowired
    private RestTemplateDelegation delegation;

    @PostConstruct
    private void init() {
        httpUtilDelegation = this.delegation;
    }

    private Map<String, RequestWrapper.Query> requests;

    public BatchRequestsProcessor(Collapser builder) {
        requests = builder.getRequests();
    }


    @Component
    public static class Collapser {
        private Map<String, RequestWrapper.Query> requests = Maps.newConcurrentMap();

        public Collapser() {
        }

        public Collapser add(RequestWrapper requestWrapper){
            RequestWrapper.Query query = requestWrapper.new Query().parse(requestWrapper.getUri(), requestWrapper.getParamMap());
            this.requests.put(requestWrapper.getHost() + "#" + requestWrapper.getUri() + "#" + requestWrapper.getParamMap(), query);
            return this;
        }

        public Map<String, RequestWrapper.Query> getRequests(){
            return this.requests;
        }

        public JSONObject trigger(){
            Map<String, Object> re = httpUtilDelegation.multiGet(this.requests);
            return new JSONObject(re);
        }
    }
}
