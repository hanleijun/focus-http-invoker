package cn.focus.dc.focusaudit.common.httputil.delegation;

import cn.focus.dc.focusaudit.common.httputil.annotation.UrlParams;
import cn.focus.dc.focusaudit.common.httputil.batch.RequestWrapper;
import cn.focus.dc.focusaudit.common.httputil.constants.Code;
import cn.focus.dc.focusaudit.common.httputil.constants.HttpConstants;
import cn.focus.dc.focusaudit.common.httputil.util.EncryptUtil;
import cn.focus.dc.focusaudit.common.httputil.util.ThreadUtil;
import cn.focus.eco.data.curator.core.RedisServiceProxy;
import cn.focus.eco.data.curator.core.util.MultiMap;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.net.URI;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;


/**
 * Copyright (C) 2015 - 2017 SOHU Inc. All Rights Reserved.
 * <p>
 * Call api based on Resttemplate
 *
 * @author legend(leijunhan@sohu-inc.com)
 * @Date: 2017-01-15
 */

public class RestTemplateDelegation<T> implements HttpUtilDelegation {

    private RedisServiceProxy redisServiceProxy;

    private AsyncRestTemplate restTemplate;

    private String hostPrefix = StringUtils.EMPTY;

    private String reqPath = StringUtils.EMPTY;

    private String paramStr = StringUtils.EMPTY;

    private static final Logger logger = Logger.getLogger(RestTemplateDelegation.class);

    public RestTemplateDelegation(AsyncRestTemplate restTemplate, RedisServiceProxy redisServiceProxy) {
        this.restTemplate = restTemplate;
        this.redisServiceProxy = redisServiceProxy;
        Assert.notNull(this.restTemplate, "restTemplate can not be null, please check your config");
    }

    @Override
    public ListenableFuture<ResponseEntity> post(URI url, HttpEntity entity, Class responseType) {
        return restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
    }

    @Override
    public ListenableFuture<ResponseEntity> put(URI url, HttpEntity entity, Class responseType) {
        return restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
    }

    public Map<String, JSONObject> multiGet(Map<String, RequestWrapper.Query> requests){
        MultiMap multiMap = new MultiMap();
        Map<String, JSONObject> hittedResultMap = Maps.newConcurrentMap();
        Map<String, ListenableFuture<ResponseEntity>> futureMap = Maps.newConcurrentMap();
        String tmp = StringUtils.substringBefore(requests.entrySet().stream().findFirst().get().getKey(), "#");
        String protocol = StringUtils.substringBefore(tmp, "//");
        String env = StringUtils.substringBetween(tmp, ".", ".cn");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity httpEntity = new HttpEntity(httpHeaders);

        requests.forEach((k, v) -> {
            k = Arrays.stream(StringUtils.substringsBetween(k, "//", "."))
                        .findFirst().get();
            String reqPath = v.getUri();
            String q = v.getQ();
            List list = Arrays.stream(StringUtils.split(q, "&"))
                    .filter(x -> !StringUtils.containsIgnoreCase(x, "appid")).collect(Collectors.toList());
            String paramStr = StringUtils.join(list, "&");
            String cacheKey = EncryptUtil.encodeRequest(k, env, reqPath, paramStr);
            multiMap.put(k, cacheKey);
        });
        multiMap.getAll().forEach((k, v) ->{
                    List vList = (List)redisServiceProxy.mGet(k.toString(), (List)v);
                    for(int i = 0; i < vList.size(); i++){
                        String decodeUrl = EncryptUtil.decodeBase64(((List) v).get(i).toString());
                        String[] params = StringUtils.split(decodeUrl, EncryptUtil.SPLITTER);
                        String url = protocol + "//" + params[0] + "." + params[1] + ".cn" + params[2];
                        if(params.length == 4){
                            url += "?" + params[3];
                        }
                        if(vList.get(i) == null){
                            try {
                                URI uri = new URI(url);
                                ListenableFuture<ResponseEntity> re = get(uri, httpEntity, String.class);
                                futureMap.put(url, re);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else{
                            JSONObject cachedValue = new JSONObject((Map<String, Object>) vList.get(i));
                            JSONObject realCache = JSONObject.parseObject(cachedValue.getString("value"));
                            hittedResultMap.put(url, realCache);
                        }
                    }
                    futureMap.forEach((x, y) -> {
                        try {
                            JSONObject re = JSONObject.parseObject(y.get(6, TimeUnit.SECONDS).getBody().toString());
                            hittedResultMap.put(x, re);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            Map re = Maps.newConcurrentMap();
                            re.put("msg", "timeout");
                            hittedResultMap.put(x, new JSONObject(re));
                            e.printStackTrace();
                        }
                    });
                }
            );
        return hittedResultMap;
    }

    /**
     * 对于带有{@link HttpConstants.CACHE_PATH} 且参数使用{@link UrlParams}注解传入参数的优先进行缓存碎片的检测 如果有缓存直接返回
     * 如果没有进行http调用 同时异步回写缓存
     * @param url
     * @param entity
     * @param responseType
     * @return
     */
    @Override
    public ListenableFuture<ResponseEntity> get(URI url, HttpEntity entity, Class responseType) throws ExecutionException, InterruptedException {
        hostPrefix = Arrays.stream(StringUtils.substringsBetween(url.toString(), "//", "."))
                .findFirst().get();
        reqPath = url.getPath();
        String env = StringUtils.substringBetween(url.toString(), ".", ".cn");
        if(url.getQuery() != null){
            List list = Arrays.stream(StringUtils.split(url.getQuery(), "&"))
                    .filter(x -> !StringUtils.containsIgnoreCase(x, "appid")).collect(Collectors.toList());
            paramStr = StringUtils.join(list, "&");
        }
        String cacheKey = EncryptUtil.encodeRequest(hostPrefix, env, reqPath, paramStr);

        if(StringUtils.contains(url.toString(), HttpConstants.CACHE_PATH) &&
                StringUtils.isNotBlank(cacheKey)){
            // 禁止批量取值接口
            if(StringUtils.contains(url.toString(), HttpConstants.COMMA_CODE)){
                try {
                    throw new ParseException("url should not contains comma or list", Code.ERROR_PARAMS_STYLE_WRONG);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            LinkedHashMap map = (LinkedHashMap) redisServiceProxy.get(hostPrefix, cacheKey);
            if(map != null){
                JSONObject jo  = new JSONObject(map);
                if(jo.containsKey("exTime") && jo.containsKey("value")){
                    // 包含有效的缓存
                    Long now = System.currentTimeMillis();
                    Long exTime = jo.getLong("exTime");

                    ResponseEntity re = new ResponseEntity(jo.getString("value"), entity.getHeaders(), HttpStatus.OK);
                    FutureWrapper wrapper = new FutureWrapper(re);

                    if(now > exTime){
                        ThreadUtil.NORMAL_THREAD_EXECUTOR.execute(() ->updateCache(url,entity,responseType,cacheKey));
                    }
                    return wrapper;
                }

            }
            ThreadUtil.NORMAL_THREAD_EXECUTOR.execute(() ->updateCache(url,entity,responseType,cacheKey));

        }

        ListenableFuture<ResponseEntity> re = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
        re.addCallback(
                ok -> logger.info("invoke sucuccess url:" + url + " method: GET" + " entity header:" + entity.getHeaders() +
                        " entity body:" + entity.getBody()),
                ex -> logger.error("invoke failed url:" + url + " method: GET" + " entity header:" + entity.getHeaders() +
                        " entity body:" + entity.getBody())
        );
        return re;
    }

    @Override
    public ListenableFuture<ResponseEntity> delete(URI url, HttpEntity entity, Class responseType) {
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, responseType);
    }

    @Override
    public ListenableFuture<ResponseEntity> options(URI url, HttpEntity entity, Class responseType) {
        return restTemplate.exchange(url, HttpMethod.OPTIONS, entity, responseType);
    }

    @Override
    public ListenableFuture<ResponseEntity> head(URI url, HttpEntity entity, Class responseType) {
        return restTemplate.exchange(url, HttpMethod.HEAD, entity, responseType);
    }

    @Override
    public ListenableFuture<ResponseEntity> patch(URI url, HttpEntity entity, Class responseType) {
        return restTemplate.exchange(url, HttpMethod.PATCH, entity, responseType);
    }

    @Override
    public ListenableFuture<ResponseEntity> trace(URI url, HttpEntity entity, Class responseType) {
        return restTemplate.exchange(url, HttpMethod.TRACE, entity, responseType);
    }

    private void updateCache(URI url, HttpEntity entity, Class responseType, String cacheKey ){

        if(lock(cacheKey)) {
            ListenableFuture<ResponseEntity> re = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
            try {
                Object value = re.get().getBody();
                long expireTime = System.currentTimeMillis() + HttpConstants.CACHE_TIME;

                Map<String, Object> v = new HashMap<>(2);
                v.put("exTime", expireTime);
                v.put("value", value);

                redisServiceProxy.set(hostPrefix, cacheKey, v);
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e);
            } finally {
                unlock(cacheKey);
            }
        }
    }

    private boolean lock(String cacheKey){
        long now = System.currentTimeMillis();
        String key = cacheKey + ":lock";
        if(redisServiceProxy.setNx(hostPrefix, key,System.currentTimeMillis() + HttpConstants.LOCK_TIME)){
            redisServiceProxy.expire(hostPrefix,key,HttpConstants.LOCK_TIME_SECOND);
            return true;
        }else {
            Long time = NumberUtils.toLong(redisServiceProxy.get(hostPrefix, key).toString());
            if(time < now){
                // 可能存在死锁情况，需要手动解锁
                redisServiceProxy.delete(hostPrefix,key);
                if(redisServiceProxy.setNx(hostPrefix,key,System.currentTimeMillis() + HttpConstants.LOCK_TIME)){
                    redisServiceProxy.expire(hostPrefix,key,HttpConstants.LOCK_TIME_SECOND);
                    return true;
                }
            }
        }
        return false;
    }

    private void unlock(String cacheKey){
        String key = cacheKey + ":lock";
        redisServiceProxy.delete(hostPrefix,key);
    }

    public static void main(String[] args) {
        String test = EncryptUtil.encodeRequest("house-sv-base", "focus-dev","city/list", " ");
        System.out.println(test);
    }
}
