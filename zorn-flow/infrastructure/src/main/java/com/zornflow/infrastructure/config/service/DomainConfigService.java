package com.zornflow.infrastructure.config.service;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.infrastructure.config.cache.ConfigCacheManager;
import com.zornflow.infrastructure.config.source.ConfigSourceManager;
import com.zornflow.infrastructure.config.source.contract.ConfigSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 领域配置服务
 * 基于缓存的高性能实现，支持配置的预加载和热更新
 * <p>
 * 设计特性：
 * - 基于缓存的高性能访问
 * - 支持配置预加载和热更新
 * - 线程安全的并发访问
 * - 失败容错机制
 * <p>
 * 性能优化：
 * - 读操作无锁化，极高性能
 * - 预转换的领域对象，避免运行时转换
 * - 原子性更新，保证数据一致性
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 2.0
 * @since 2025/8/29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DomainConfigService {

  private final ConfigCacheManager cacheManager;
  private final ConfigSourceManager configSourceManager;
  private final ConfigPreloadService preloadService;

  // ============================= 规则链查询 =============================

  /**
   * 根据ID查找规则链（从缓存）
   *
   * @param chainId 规则链ID
   * @return 规则链的Optional包装，不存在时返回empty
   */
  public Optional<RuleChain> findRuleChain(String chainId) {
    if (chainId == null || chainId.isBlank()) {
      log.debug("Rule chain ID is null or blank, returning empty");
      return Optional.empty();
    }

    log.debug("Finding rule chain by ID: {}", chainId);
    return cacheManager.getRuleChain(chainId);
  }

  /**
   * 根据强类型ID查找规则链
   *
   * @param chainId 规则链ID值对象
   * @return 规则链的Optional包装
   */
  public Optional<RuleChain> findRuleChain(RuleChainId chainId) {
    return chainId != null ? findRuleChain(chainId.value()) : Optional.empty();
  }

  /**
   * 获取所有规则链（从缓存）
   *
   * @return 所有可用的规则链列表
   */
  public List<RuleChain> findAllRuleChains() {
    log.debug("Loading all rule chains from cache");
    List<RuleChain> result = cacheManager.getAllRuleChains();
    log.debug("Loaded {} rule chains from cache", result.size());
    return result;
  }

  // ============================= 流程链查询 =============================

  /**
   * 根据ID查找流程链（从缓存）
   *
   * @param chainId 流程链ID
   * @return 流程链的Optional包装
   */
  public Optional<ProcessChain> findProcessChain(String chainId) {
    if (chainId == null || chainId.isBlank()) {
      log.debug("Process chain ID is null or blank, returning empty");
      return Optional.empty();
    }

    log.debug("Finding process chain by ID: {}", chainId);
    return cacheManager.getProcessChain(chainId);
  }

  /**
   * 根据强类型ID查找流程链
   *
   * @param chainId 流程链ID值对象
   * @return 流程链的Optional包装
   */
  public Optional<ProcessChain> findProcessChain(ProcessChainId chainId) {
    return chainId != null ? findProcessChain(chainId.value()) : Optional.empty();
  }

  /**
   * 获取所有流程链（从缓存）
   *
   * @return 所有可用的流程链列表
   */
  public List<ProcessChain> findAllProcessChains() {
    log.debug("Loading all process chains from cache");
    List<ProcessChain> result = cacheManager.getAllProcessChains();
    log.debug("Loaded {} process chains from cache", result.size());
    return result;
  }

  // ============================= 配置管理 =============================

  /**
   * 安全刷新配置缓存
   * 支持原子性更新和失败容错
   */
  public ConfigPreloadService.RefreshResult refreshConfiguration() {
    log.info("Refreshing configuration cache");

    try {
      return preloadService.refreshConfigurations();
    } catch (Exception e) {
      log.error("Failed to refresh configuration: {}", e.getMessage(), e);
      throw new ConfigurationException("配置刷新失败", e);
    }
  }

  /**
   * 验证配置完整性
   *
   * @return 配置验证结果
   */
  public ConfigValidationResult validateConfiguration() {
    log.debug("Validating configuration integrity");

    try {
      ConfigCacheManager.CacheStatistics cacheStats = cacheManager.getStatistics();
      List<ConfigSource.ConfigSourceStatistics> sourceStats = configSourceManager.getStatistics();

      boolean isValid = cacheStats.ruleChainCount() > 0 || cacheStats.processChainCount() > 0;
      boolean allSourcesAvailable = sourceStats.stream().allMatch(ConfigSource.ConfigSourceStatistics::isAvailable);

      String message = String.format(
        "Configuration validation: %s (cached: %d rule chains, %d process chains, sources available: %s)",
        isValid ? "PASSED" : "FAILED",
        cacheStats.ruleChainCount(),
        cacheStats.processChainCount(),
        allSourcesAvailable
      );

      log.info(message);

      return new ConfigValidationResult(isValid && allSourcesAvailable, message);

    } catch (Exception e) {
      log.error("Configuration validation failed: {}", e.getMessage(), e);
      return new ConfigValidationResult(false, "验证失败: " + e.getMessage());
    }
  }

  /**
   * 获取配置统计信息
   *
   * @return 配置统计信息
   */
  public ConfigStatistics getStatistics() {
    try {
      ConfigCacheManager.CacheStatistics cacheStats = cacheManager.getStatistics();
      List<ConfigSource.ConfigSourceStatistics> sourceStats = configSourceManager.getStatistics();

      // 计算总规则数和节点数
      int totalRules = cacheManager.getAllRuleChains().stream()
        .mapToInt(RuleChain::getRuleCount)
        .sum();

      int totalNodes = cacheManager.getAllProcessChains().stream()
        .mapToInt(chain -> chain.getAllNodes().size())
        .sum();

      // 构建配置源信息
      StringBuilder sourceInfo = new StringBuilder("Sources: ");
      for (ConfigSource.ConfigSourceStatistics stat : sourceStats) {
        sourceInfo.append(String.format("%s(%d/%d)",
          stat.sourceName(), stat.ruleChainCount(), stat.processChainCount()));
        if (sourceStats.indexOf(stat) < sourceStats.size() - 1) {
          sourceInfo.append(", ");
        }
      }

      return new ConfigStatistics(
        cacheStats.ruleChainCount(),
        cacheStats.processChainCount(),
        totalRules,
        totalNodes,
        String.format("Cached (updated: %s) [%s]",
          new Date(cacheStats.lastUpdateTime()),
          sourceInfo)
      );

    } catch (Exception e) {
      log.error("Failed to generate configuration statistics: {}", e.getMessage(), e);
      return ConfigStatistics.empty();
    }
  }

  /**
   * 获取配置源信息
   *
   * @return 配置源统计信息列表
   */
  public List<ConfigSource.ConfigSourceStatistics> getConfigSourceStatistics() {
    return configSourceManager.getStatistics();
  }

  /**
   * 获取缓存统计信息
   *
   * @return 缓存统计信息
   */
  public ConfigCacheManager.CacheStatistics getCacheStatistics() {
    return cacheManager.getStatistics();
  }

  // ============================= 异常处理 =============================

  /**
   * 配置异常
   */
  public static class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
      super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * 配置验证结果
   */
  public record ConfigValidationResult(
    boolean isValid,
    String message
  ) {
  }

  /**
   * 配置统计信息
   */
  public record ConfigStatistics(
    int ruleChainCount,
    int processChainCount,
    int totalRules,
    int totalNodes,
    String status
  ) {
    public static ConfigStatistics empty() {
      return new ConfigStatistics(0, 0, 0, 0, "Empty");
    }
  }
}
