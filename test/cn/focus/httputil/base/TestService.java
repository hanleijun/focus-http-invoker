package cn.focus.httputil.base;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author chengruiwang
 * @version 1.0
 * @since 2018-01-30
 */
@Service
public class TestService {

    @Value("${host.house-sv-base}")
    private String houseSvBaseHost;

    @Autowired
    ChatHttpUtil chatHttpUtil;

    @Autowired
    private ProjectHttpUtil projectHttpUtil;

    public String getHouseSvBaseHost() {
        return houseSvBaseHost;
    }

    public void setHouseSvBaseHost(String houseSvBaseHost) {
        this.houseSvBaseHost = houseSvBaseHost;
    }

    public void printChat(){
        Map<String,Object> param = new HashMap<>(1);
        param.put("uid","144027801");
        ListenableFuture<ResponseEntity<String>> jo = chatHttpUtil.getChatInfo("http://u.focus.cn", JSON.parseObject(JSON.toJSONString(param)));

        try {
            System.out.println(jo.get().getBody());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void printProjectInfo(){
        Map<String,Object> param = new HashMap<>(1);
        param.put("pid",1);
        ListenableFuture<ResponseEntity<String>> jo = projectHttpUtil
                .getProjectInfo(houseSvBaseHost, JSON.parseObject(JSON.toJSONString(param)));

        try {
            //调用get()方法时会阻塞的去获取接口返回数据
            System.out.println(jo.get().getBody());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
