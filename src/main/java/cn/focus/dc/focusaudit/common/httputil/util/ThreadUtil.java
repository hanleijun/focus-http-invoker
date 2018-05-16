package cn.focus.dc.focusaudit.common.httputil.util;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Copyright (C) 1998 - 2017 SOHU Inc., All Rights Reserved.
 * <p>
 *
 * @author: leijunhan (leijunhan@sohu-inc.com)
 * @date: 2018/1/17
 */
public class ThreadUtil {
    public static ScheduledExecutorService NORMAL_THREAD_EXECUTOR = new ScheduledThreadPoolExecutor(30,
            new BasicThreadFactory.Builder().namingPattern("http-invoker-cache-pool-%d").daemon(true).build());
}
