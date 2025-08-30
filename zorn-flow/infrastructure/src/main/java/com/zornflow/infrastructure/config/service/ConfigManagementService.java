package com.zornflow.infrastructure.config.service;

import com.zornflow.infrastructure.config.source.ReadWriteConfigSource;
import com.zornflow.infrastructure.config.repository.CompositeProcessChainRepository;
import com.zornflow.infrastructure.config.repository.CompositeRuleChainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 配置管理服务
 * 负责配置的刷新、缓存管理和预热等操作
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "zornflow.config.management.enabled", havingValue = "true", matchIfMissing = true)
public class ConfigManagementService {

  private final ReadWriteConfigSource configSource;
  private final CompositeRuleChainRepository ruleChainRepository;
  private final CompositeProcessChainRepository processChainRepository;

  // 使用原子变量替代锁，提高性能
  private final AtomicBoolean refreshing = new AtomicBoolean(false);
  private final AtomicLong lastRefreshTime = new AtomicLong(0);

  /**
   * 手动刷新所有配置（无锁优化版本）
   *
   * @return 刷新是否成功
   */
  public boolean refreshAllConfigs() {
    // 使用CAS操作替代锁
    if (!refreshing.compareAndSet(false, true)) {
      log.info("配置刷新正在进行中，跳过本次请求");
      return false;
    }

    try {
      long startTime = System.currentTimeMillis();
      log.info("开始刷新所有配置...");

      // 1. 刷新配置源
      boolean sourceRefreshResult = configSource.refresh();
      if (!sourceRefreshResult) {
        log.warn("配置源刷新失败");
        return false;
      }

      // 2. 清空Repository缓存
      ruleChainRepository.clearCache();
      processChainRepository.clearCache();

      // 3. 预热缓存
      preloadConfigs();

      long endTime = System.currentTimeMillis();
      lastRefreshTime.set(endTime);
      long duration = endTime - startTime;

      log.info("配置刷新完成，耗时: {} ms", duration);
      return true;

    } catch (Exception e) {
      log.error("配置刷新失败", e);
      return false;
    } finally {
      refreshing.set(false);
    }
  }

  /**
   * 异步刷新配置
   *
   * @return 异步结果
   */
  @Async
  public CompletableFuture<Boolean> refreshAllConfigsAsync() {
    return CompletableFuture.supplyAsync(this::refreshAllConfigs);
  }

  /**
   * 定时刷新配置（可通过配置启用）
   */
  @Scheduled(fixedDelayString = "${zornflow.config.refresh.interval:600000}") // 默认10分钟
  @ConditionalOnProperty(name = "zornflow.config.refresh.scheduled", havingValue = "true")
  public void scheduledRefresh() {
    if (refreshing.get()) {
      log.debug("配置刷新正在进行中，跳过定时刷新");
      return;
    }

    log.info("执行定时配置刷新");
    refreshAllConfigs();
  }

  /**
   * 预加载和预热缓存
   */
  public void preloadConfigs() {
    try {
      log.info("开始预热配置缓存...");

      // 预加载规则链
      var ruleChains = ruleChainRepository.findAll();
      log.debug("预加载规则链: {} 个", ruleChains.size());

      // 预加载流程链
      var processChains = processChainRepository.findAll();
      log.debug("预加载流程链: {} 个", processChains.size());

      log.info("配置缓存预热完成");

    } catch (Exception e) {
      log.error("配置缓存预热失败", e);
    }
  }

  /**
   * 清空所有缓存
   */
  public void clearAllCaches() {
    try {
      log.info("清空所有配置缓存...");

      configSource.clearCache();
      ruleChainRepository.clearCache();
      processChainRepository.clearCache();

      log.info("配置缓存清空完成");

    } catch (Exception e) {
      log.error("清空配置缓存失败", e);
    }
  }

  /**
   * 获取配置源状态信息
   */
  public ConfigSourceStatus getConfigSourceStatus() {
    return ConfigSourceStatus.builder()
      .available(configSource.isAvailable())
      .refreshing(refreshing.get())
      .lastRefreshTime(lastRefreshTime.get())
      .sourceName(configSource.getSourceName())
      .sourceType(configSource.getSourceType().name())
      .priority(configSource.getPriority())
      .build();
  }

  /**
   * 检查配置源健康状态
   */
  public boolean isHealthy() {
    try {
      return configSource.isAvailable() &&
        ruleChainRepository.isAvailable() &&
        processChainRepository.isAvailable();
    } catch (Exception e) {
      log.error("检查配置源健康状态失败", e);
      return false;
    }
  }

  /**
   * 配置源状态信息
   */
  public static class ConfigSourceStatus {
    private final boolean available;
    private final boolean refreshing;
    private final long lastRefreshTime;
    private final String sourceName;
    private final String sourceType;
    private final int priority;

    private ConfigSourceStatus(boolean available, boolean refreshing, long lastRefreshTime,
                               String sourceName, String sourceType, int priority) {
      this.available = available;
      this.refreshing = refreshing;
      this.lastRefreshTime = lastRefreshTime;
      this.sourceName = sourceName;
      this.sourceType = sourceType;
      this.priority = priority;
    }

    public static ConfigSourceStatusBuilder builder() {
      return new ConfigSourceStatusBuilder();
    }

    // Getters
    public boolean isAvailable() {
      return available;
    }

    public boolean isRefreshing() {
      return refreshing;
    }

    public long getLastRefreshTime() {
      return lastRefreshTime;
    }

    public String getSourceName() {
      return sourceName;
    }

    public String getSourceType() {
      return sourceType;
    }

    public int getPriority() {
      return priority;
    }

    public static class ConfigSourceStatusBuilder {
      private boolean available;
      private boolean refreshing;
      private long lastRefreshTime;
      private String sourceName;
      private String sourceType;
      private int priority;

      public ConfigSourceStatusBuilder available(boolean available) {
        this.available = available;
        return this;
      }

      public ConfigSourceStatusBuilder refreshing(boolean refreshing) {
        this.refreshing = refreshing;
        return this;
      }

      public ConfigSourceStatusBuilder lastRefreshTime(long lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
        return this;
      }

      public ConfigSourceStatusBuilder sourceName(String sourceName) {
        this.sourceName = sourceName;
        return this;
      }

      public ConfigSourceStatusBuilder sourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
      }

      public ConfigSourceStatusBuilder priority(int priority) {
        this.priority = priority;
        return this;
      }

      public ConfigSourceStatus build() {
        return new ConfigSourceStatus(available, refreshing, lastRefreshTime, sourceName, sourceType, priority);
      }
    }
  }
}
