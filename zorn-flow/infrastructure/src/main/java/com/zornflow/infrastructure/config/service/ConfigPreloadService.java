package com.zornflow.infrastructure.config.service;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.infrastructure.config.cache.ConfigCacheManager;
import com.zornflow.infrastructure.config.converter.ConfigConverter;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.source.ConfigSourceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 配置预加载服务
 * <p>
 * 职责：
 * - 系统启动时预加载所有配置
 * - 将配置模型转换为领域对象并缓存
 * - 提供安全的配置刷新机制
 * - 监控配置加载性能
 * <p>
 * 设计特性：
 * - 实现ApplicationRunner，在系统启动完成后自动加载
 * - 异步加载提高启动性能
 * - 原子性刷新保证数据一致性
 * - 失败容错机制，不影响系统启动
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigPreloadService implements ApplicationRunner {

  // 配置加载超时时间（秒）
  private static final int LOAD_TIMEOUT_SECONDS = 30;
  private final ConfigSourceManager configSourceManager;
  private final ConfigConverter converter;
  private final ConfigCacheManager cacheManager;

  @Override
  public void run(ApplicationArguments args) {
    log.info("Starting configuration preload...");

    try {
      // 异步预加载配置，避免阻塞系统启动
      CompletableFuture<Void> preloadFuture = CompletableFuture.runAsync(this::preloadAllConfigurations);

      // 设置超时，避免无限等待
      preloadFuture.get(LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);

      log.info("Configuration preload completed successfully");

    } catch (TimeoutException e) {
      log.error("Configuration preload timed out after {} seconds, system will continue with empty cache",
        LOAD_TIMEOUT_SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Configuration preload was interrupted, system will continue with empty cache");
    } catch (ExecutionException e) {
      log.error("Configuration preload failed, system will continue with empty cache", e.getCause());
    }
  }

  /**
   * 预加载所有配置
   */
  public void preloadAllConfigurations() {
    long startTime = System.currentTimeMillis();

    try {
      log.info("Preloading configurations from all sources...");

      // 并行加载规则链和流程链配置
      CompletableFuture<Map<String, RuleChainConfig>> ruleChainsFuture =
        CompletableFuture.supplyAsync(this::loadAllRuleChainConfigs);

      CompletableFuture<Map<String, ProcessChainConfig>> processChainsFuture =
        CompletableFuture.supplyAsync(this::loadAllProcessChainConfigs);

      // 等待两个任务完成
      Map<String, RuleChainConfig> ruleChainConfigs = ruleChainsFuture.join();
      Map<String, ProcessChainConfig> processChainConfigs = processChainsFuture.join();

      log.info("Loaded {} rule chain configs and {} process chain configs",
        ruleChainConfigs.size(), processChainConfigs.size());

      // 并行转换为领域对象
      CompletableFuture<Map<String, RuleChain>> ruleChainsFutureConverted =
        CompletableFuture.supplyAsync(() -> convertRuleChains(ruleChainConfigs));

      CompletableFuture<Map<String, ProcessChain>> processChainsFutureConverted =
        CompletableFuture.supplyAsync(() -> convertProcessChains(processChainConfigs));

      // 等待转换完成
      Map<String, RuleChain> ruleChains = ruleChainsFutureConverted.join();
      Map<String, ProcessChain> processChains = processChainsFutureConverted.join();

      log.info("Converted {} rule chains and {} process chains to domain objects",
        ruleChains.size(), processChains.size());

      // 原子更新缓存
      ConfigCacheManager.ConfigSnapshot newSnapshot = new ConfigCacheManager.ConfigSnapshot(
        ruleChainConfigs,
        processChainConfigs,
        ruleChains,
        processChains,
        System.currentTimeMillis()
      );

      cacheManager.updateCache(newSnapshot);

      long duration = System.currentTimeMillis() - startTime;
      log.info("Configuration preload completed in {} ms", duration);

    } catch (Exception e) {
      log.error("Failed to preload configurations", e);
      throw e;
    }
  }

  /**
   * 安全刷新配置缓存
   * 支持运行时动态刷新，保证原子性和失败容错
   */
  public RefreshResult refreshConfigurations() {
    log.info("Starting configuration refresh...");
    long startTime = System.currentTimeMillis();

    try {
      // 先刷新所有配置源
      configSourceManager.refreshAll();

      // 重新加载配置（使用备份机制）
      ConfigCacheManager.ConfigSnapshot backupSnapshot = null;

      try {
        // 备份当前缓存
        backupSnapshot = getCurrentSnapshot();

        // 重新加载
        preloadAllConfigurations();

        long duration = System.currentTimeMillis() - startTime;
        log.info("Configuration refresh completed successfully in {} ms", duration);

        return new RefreshResult(true,
          String.format("Refresh completed in %d ms", duration),
          cacheManager.getStatistics());

      } catch (Exception e) {
        // 刷新失败，恢复备份
        if (backupSnapshot != null) {
          log.warn("Configuration refresh failed, restoring backup cache", e);
          cacheManager.updateCache(backupSnapshot);
          return new RefreshResult(false,
            "Refresh failed, backup restored: " + e.getMessage(),
            cacheManager.getStatistics());
        } else {
          throw e;
        }
      }

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Configuration refresh failed after {} ms", duration, e);
      return new RefreshResult(false,
        "Refresh failed: " + e.getMessage(),
        cacheManager.getStatistics());
    }
  }

  // ============================= 私有方法 =============================

  private Map<String, RuleChainConfig> loadAllRuleChainConfigs() {
    try {
      List<RuleChainConfig> configs = configSourceManager.findAllRuleChains();
      return configs.stream()
        .collect(Collectors.toMap(
          RuleChainConfig::id,
          config -> config,
          (existing, replacement) -> replacement // 新配置覆盖旧配置
        ));
    } catch (Exception e) {
      log.error("Failed to load rule chain configs", e);
      return Collections.emptyMap();
    }
  }

  private Map<String, ProcessChainConfig> loadAllProcessChainConfigs() {
    try {
      List<ProcessChainConfig> configs = configSourceManager.findAllProcessChains();
      return configs.stream()
        .collect(Collectors.toMap(
          ProcessChainConfig::id,
          config -> config,
          (existing, replacement) -> replacement
        ));
    } catch (Exception e) {
      log.error("Failed to load process chain configs", e);
      return Collections.emptyMap();
    }
  }

  private Map<String, RuleChain> convertRuleChains(Map<String, RuleChainConfig> configs) {
    return configs.entrySet().stream()
      .map(entry -> {
        try {
          return converter.convertRuleChain(entry.getValue())
            .map(ruleChain -> Map.entry(entry.getKey(), ruleChain))
            .orElse(null);
        } catch (Exception e) {
          log.warn("Failed to convert rule chain: {}", entry.getKey(), e);
          return null;
        }
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue
      ));
  }

  private Map<String, ProcessChain> convertProcessChains(Map<String, ProcessChainConfig> configs) {
    return configs.entrySet().stream()
      .map(entry -> {
        try {
          return converter.convertProcessChain(entry.getValue())
            .map(processChain -> Map.entry(entry.getKey(), processChain))
            .orElse(null);
        } catch (Exception e) {
          log.warn("Failed to convert process chain: {}", entry.getKey(), e);
          return null;
        }
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue
      ));
  }

  private ConfigCacheManager.ConfigSnapshot getCurrentSnapshot() {
    return new ConfigCacheManager.ConfigSnapshot(
      cacheManager.getAllRuleChainConfigs().stream()
        .collect(Collectors.toMap(RuleChainConfig::id, config -> config)),
      cacheManager.getAllProcessChainConfigs().stream()
        .collect(Collectors.toMap(ProcessChainConfig::id, config -> config)),
      cacheManager.getAllRuleChains().stream()
        .collect(Collectors.toMap(chain -> chain.getId().value(), chain -> chain)),
      cacheManager.getAllProcessChains().stream()
        .collect(Collectors.toMap(chain -> chain.getId().value(), chain -> chain)),
      System.currentTimeMillis()
    );
  }

  // ============================= 结果类型 =============================

  /**
   * 刷新结果
   */
  public record RefreshResult(
    boolean success,
    String message,
    ConfigCacheManager.CacheStatistics statistics
  ) {
  }
}
