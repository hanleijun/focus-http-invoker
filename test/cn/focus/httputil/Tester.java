package cn.focus.httputil;

import cn.focus.dc.focusaudit.common.httputil.batch.BatchRequestsProcessor;
import cn.focus.dc.focusaudit.common.httputil.batch.RequestWrapper;
import cn.focus.eco.data.curator.core.RedisServiceProxy;
import cn.focus.httputil.base.Application;
import cn.focus.httputil.base.TestService;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @author chengruiwang
 * @version 1.0
 * @since 2018-01-30
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class Tester {
    @Autowired
    private TestService testService;
    @Autowired
    private BatchRequestsProcessor.Collapser collapser;
    @Autowired
    private RedisServiceProxy redisServiceProxy;

    @Test
    public void test(){
        for(int i =15;i<25;i++){
            Map map = new HashMap();
            map.put("cityId", i);
            map.put("source", 1);
            RequestWrapper wrapper = new RequestWrapper("http://house-sv-base.focus.cn", "/city/getCityDirectoryByCityId", map);
            collapser.add(wrapper);
        }
        long start = System.currentTimeMillis();
        JSONObject results = collapser.trigger();
        long end = System.currentTimeMillis();
        System.out.println("time:---------->"+ (end - start));
        System.out.println(results);
    }

    @Test
    public void test1(){
        testService.printChat();
    }

    @Test
    public void test2(){
        List<String> list = Lists.newArrayList();
        list.add("keya");
        list.add("keys");
        list.add("keyb");
        list.add("keyt");
        List obj = (List)redisServiceProxy.mGet("house-sv-base", list);
        System.out.println(obj);

    }
}
