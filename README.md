# focus-http-invoker
**此组件主要功能**

1 统一http异步调用方式

2 屏蔽底层缓存碎片治理逻辑

3 主动加入appId，便于追溯调用来源

4 简明的API使用

5 将无限的网络请求折叠成有限的请求规模


![image](https://upload-images.jianshu.io/upload_images/9243571-52bff4e1018ec389.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

引入依赖

```
<dependency>
  <groupId>cn.focus.eco.house</groupId>
  <artifactId>focus-http-invoker</artifactId>
  <version>1.1-SNAPSHOT</version>
</dependency>
```

定义扫描路径("cn.focus")

```
/**
 * 在SpringBootStarter上面加上扫描定义
 */
@SpringBootApplication(scanBasePackages = "cn.focus")

```

加入zookeepr地址（测试环境与正式环境对应不同监听）

```
spring:
       zk:
          zkHost: ${ip}:${port}

```

加入appId标识

```
custom:
  appId: xxxxx

```
API


注解名称
 释义	 是否必用
HttpUtil
将调用组标识为invoker触发
是
Api
将标记了{@link HttpUtil}的接口中的抽象方法标记为http调用接口
是
Host
调用url的host地址，如：http://house-sv-base.focus.cn
是
UrlParams
必须为Map子类，否则不做解析，如：params = {key1:value1}
否
UrlVariable
将参数标记为 Url上的占位符替换数据，如：http://www.baidu.com/{one}，params = xyz
否
@Api 注解的参数释义：



参数名称	参数释义
url	
指定访问url，若方法的参数中没有{@link Host}标记的参数,此url会直接被访问，
若方法的参数中有{@link Host}标记的参数，此url会拼接在标记有{@link Host}的参数值后方(如果有多个Host标记，取第一个)
method	
指定访问方法,默认GET方法访问，其他的有：POST, DELETE, PUT, OPTIONS, HEAD, PATCH, TRACE
headerBuilder	
指定请求中的header信息生成类,默认生成类请参考{@link DefaultHeaderBuilder}
 
上述headerBuilder的自定义实现方式举例：
```
public class ConfigJSONHeaderBuilder implements HeaderBuilder {

    @Override
    public HttpHeaders build() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        return headers;
    }
}
```



应用举例


```
@HttpUtil
public interface GeoConfigHttpUtil {

    @Api(url = "/city/editContactPerson", method = HttpMethod.POST, headerBuilder = ConfigJSONHeaderBuilder.class)
    JSONObject editContactPerson(@Host String host, @BodyContent(BodyType.JSON) JSONObject jsonObject);

    @Api(url = "/city/deleteCircle", method = HttpMethod.POST, headerBuilder = ConfigFormHeaderBuilder.class)
    JSONObject deleteCircle(@Host String host,
                            @BodyContent(BodyType.FORM) JSONObject param);
 
    @Api(url = "/city/getAllVdistrictByCityId", method = HttpMethod.GET)
    JSONObject getAllVdistrictByCityId(@Host String host, @UrlParams JSONObject param);
 
    @Api(url = "/city/getQRCodeList?cityId={cityId}")
    JSONObject getQRCodeList(@Host String host, @UrlVariable Integer cityId);
}
```
注意：其中jar层拉取data层数据的时候，必须使用@UrlParams组装参数，且需要确保url的path中带有“/wc/”部分，

其他http调用，按需索取即可

在需要使用该第三方调用的数据地方，如下引入接口使用
```
@Autowired
private GeographyHttpUtil geographyHttpUtil;
```

```
JSONObject r = geographyHttpUtil.getCircleData(PROJECT_BASIC_HOST);
```

-----------------------------------v1.1.1----------------------------------------------------
1 引入依赖
```
<!-- http-invoker -->
<dependency>
    <groupId>cn.focus.eco.house</groupId>
    <artifactId>focus-http-invoker</artifactId>
    <version>1.1.1-SNAPSHOT</version>
</dependency>
```

2 异步化特性
更新说明：
1. 所有调用都走异步调用

2. 接口缓存保存10分钟，缓存过期后的第一个请求负责异步的更新缓存，在缓存被更新之前仍然返回旧缓存。



备注：使用分布式锁来保证只有一个请求负责更新缓存，如果出现某些异常导致未能解锁，该锁会在设置锁的10秒钟之后自动过期。



应用举例：
接口示例
```
@HttpUtil
public interface ProjectHttpUtil {

    @Api(url = "/wc/getProjectHeaderInfo",method = HttpMethod.GET)
    ListenableFuture<ResponseEntity<String>> getProjectInfo(@Host String host, @UrlParams JSONObject param);
}
```
调用示例
```
public void printProjectInfo(){
    Map<String,Object> param = new HashMap<>(1);
    param.put("pid",1);
    ListenableFuture<ResponseEntity<String>> jo = projectHttpUtil.getProjectInfo(houseSvBaseHost, JSON.parseObject(JSON.toJSONString(param)));

    try {
        //调用get()方法时会阻塞的去获取接口返回数据
        System.out.println(jo.get().getBody());
    } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
    }
}
```

请求折叠特性
更新说明：
1. 对于带有“/wc/”焦点域名的接口调用进行请求折叠，大大降低调用时延，如果其中某个请求时间超过6s，则返回{"msg":"timeout"}，对于需要走集中缓存的接口，禁止使用getInfoByIds这类接口，有批量请求走请求折叠的方式

2. API采用链式调用

调用示例
```
@Autowired
private BatchRequestsProcessor.Collapser collapser;   // 引入请求折叠器
```
 
 ```
@Test
public void test(){
    for(int i =15;i<25;i++){
        Map map = new HashMap();
        map.put("cityId", i);
        map.put("source", 1);
        RequestWrapper wrapper = new RequestWrapper("http://house-sv-base.focus.cn", "/city/getCityDirectoryByCityId", map);
        collapser.add(wrapper);                      // 将同一批才需要调用的请求不断添加到折叠器当中
    }
    long start = System.currentTimeMillis();
    JSONObject results = collapser.trigger();        // 触发批量请求
    long end = System.currentTimeMillis();
    System.out.println("time:---------->"+ (end - start));
    System.out.println(results);
}
```
注意：1.如果没有参数传入null，2.其中域名和请求路径要写全，如要写成"/city/list"而不是"city/list"，3.所调用接口全部需要为标准JSON。
结果例：


```
{
  "http://house-sv-base.focus.cn/city/getCityDirectoryByCityId?cityId=15&source=1": {
    "code": 1,
    "data": [
      {
        "cityId": 15,
        "createTime": 1505201769000,
        "creator": 0,
        "displayIndex": 0,
        "editable": 0,
        "editor": 0,
        "icon": "t.focus-res.cn/front-h5/module/city-nav/images/new-house.png",
        "id": 281,
        "source": 1,
        "status": 1,
        "subtitle": "",
        "title": "新房",
        "updateTime": 1505201769000,
        "url": "https://m.focus.cn/datong/loupan/"
      }
    ],
    "msg": "操作成功"
  },
  "http://house-sv-base.focus.cn/city/getCityDirectoryByCityId?cityId=16&source=1": {
    "code": 1,
    "data": [
      
    ],
    "msg": "操作成功"
  },
  "http://house-sv-base.focus.cn/city/getCityDirectoryByCityId?cityId=17&source=1": {
    "code": 1,
    "data": [
      {
        "cityId": 17,
        "createTime": 1505201770000,
        "creator": 0,
        "displayIndex": 0,
        "editable": 0,
        "editor": 0,
        "icon": "t.focus-res.cn/front-h5/module/city-nav/images/new-house.png",
        "id": 301,
        "source": 1,
        "status": 1,
        "subtitle": "",
        "title": "新房",
        "updateTime": 1505201770000,
        "url": "https://m.focus.cn/changzhi/loupan/"
      }
    ],
    "msg": "操作成功"
  },
  "http://house-sv-base.focus.cn/city/getCityDirectoryByCityId?cityId=24&source=1": {
    "code": 1,
    "data": [
      
    ],
    "msg": "操作成功"
  }
}
```

