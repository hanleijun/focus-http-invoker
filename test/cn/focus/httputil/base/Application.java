package cn.focus.httputil.base;

import cn.focus.dc.focusaudit.common.httputil.batch.BatchRequestsProcessor;
import cn.focus.eco.data.curator.core.SpringUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@SpringBootApplication(scanBasePackages = "cn.focus")
@Import({BatchRequestsProcessor.class, SpringUtil.class})
public class Application {
}
