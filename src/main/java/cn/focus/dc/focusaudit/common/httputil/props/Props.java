package cn.focus.dc.focusaudit.common.httputil.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Copyright (C) 1998 - 2016 SOHU Inc., All Rights Reserved.
 * <p>
 *
 * @Author: hanleijun (leijunhan@sohu-inc.com)
 * @Date: 2017/11/29
 */

@ConfigurationProperties(prefix = "custom")
public class Props {
   private String appId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
