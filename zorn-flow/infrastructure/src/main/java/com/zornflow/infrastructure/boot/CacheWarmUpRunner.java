package com.zornflow.infrastructure.boot;

import com.zornflow.infrastructure.config.source.cache.CachingProcessChainCompositeConfigSourceDecorator;
import com.zornflow.infrastructure.config.source.cache.CachingRuleChainCompositeConfigSourceDecorator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动后执行缓存预热。
 * 实现 CommandLineRunner 接口，Spring Boot 会在应用启动后自动执行 run 方法。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/1 16:51
 **/

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheWarmUpRunner implements CommandLineRunner {

  private final CachingRuleChainCompositeConfigSourceDecorator ruleCache;
  private final CachingProcessChainCompositeConfigSourceDecorator processCache;

  @Override
  public void run(String... args) {
    log.info("应用启动，开始执行配置缓存预热...");

    try {
      log.info("正在预热规则链缓存...");
      ruleCache.loadAll(); // 触发首次加载并填充缓存
      log.info("规则链缓存预热成功。");
    } catch (Exception e) {
      log.error("预热规则链缓存失败。", e);
    }

    try {
      log.info("正在预热流程链缓存...");
      processCache.loadAll(); // 触发首次加载并填充缓存
      log.info("流程链缓存预热成功。");
    } catch (Exception e) {
      log.error("预热流程链缓存失败。", e);
    }

    log.info("配置缓存预热完成。");
  }
}
