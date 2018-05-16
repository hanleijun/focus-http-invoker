package cn.focus.httputil.base;

import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;

import com.alibaba.fastjson.JSONObject;

import cn.focus.dc.focusaudit.common.httputil.annotation.Api;
import cn.focus.dc.focusaudit.common.httputil.annotation.Host;
import cn.focus.dc.focusaudit.common.httputil.annotation.HttpUtil;
import cn.focus.dc.focusaudit.common.httputil.annotation.UrlParams;
import cn.focus.dc.focusaudit.common.httputil.enums.HttpMethod;

/**
 * @author chengruiwang
 * @version 1.0
 * @since 2018-01-30
 */
@HttpUtil
public interface ProjectHttpUtil {

    @Api(url = "/wc/getProjectHeaderInfo",method = HttpMethod.GET)
    ListenableFuture<ResponseEntity<String>> getProjectInfo(@Host String host, @UrlParams JSONObject param);
}
