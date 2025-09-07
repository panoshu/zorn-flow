package com.zornflow.infrastructure.boot;

import com.zornflow.infrastructure.config.source.cache.CachingProcessChainCompositeConfigSourceDecorator;
import com.zornflow.infrastructure.config.source.cache.CachingRuleChainCompositeConfigSourceDecorator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/1 16:52
 **/

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigRefreshListener {

  private final CachingRuleChainCompositeConfigSourceDecorator ruleCache;
  private final CachingProcessChainCompositeConfigSourceDecorator processCache;

  @EventListener(ConfigRefreshEvent.class)
  public void onConfigRefresh(ConfigRefreshEvent event) {
    log.info("接收到配置刷新事件，类型: {}", event.getConfigType());

    switch (event.getConfigType()) {
      case RULES:
        ruleCache.refresh();
        break;
      case PROCESSES:
        processCache.refresh();
        break;
      case ALL:
        ruleCache.refresh();
        processCache.refresh();
        break;
    }
  }
}
