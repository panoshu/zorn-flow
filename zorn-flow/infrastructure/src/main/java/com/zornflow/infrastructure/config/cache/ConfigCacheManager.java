package com.zornflow.infrastructure.config.cache;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 线程安全的配置缓存管理器
 * 设计原则：
 * - 读写分离锁机制保证并发安全
 * - 原子引用保证缓存更新的原子性
 * - 双重缓存机制（配置模型+领域对象）
 * - 支持批量原子更新
 * 性能特性：
 * - 读操作无锁化，性能极高
 * - 写操作独占锁，保证数据一致性
 * - 内存友好地引用管理
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@Slf4j
@Component
public class ConfigCacheManager {

  // 使用原子引用保证缓存更新的原子性
  private final AtomicReference<ConfigSnapshot> currentSnapshot = new AtomicReference<>();

  // 读写锁保证并发安全
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * -- GETTER --
   * 获取缓存统计信息
   */
  // 缓存统计信息
  @Getter
  private volatile CacheStatistics statistics = new CacheStatistics(0, 0, System.currentTimeMillis());

  public ConfigCacheManager() {
    // 初始化为空缓存
    this.currentSnapshot.set(new ConfigSnapshot(
      Collections.emptyMap(),
      Collections.emptyMap(),
      Collections.emptyMap(),
      Collections.emptyMap(),
      System.currentTimeMillis()
    ));
  }

  // ============================= 配置模型访问 =============================

  /**
   * 获取规则链配置
   */
  public Optional<RuleChainConfig> getRuleChainConfig(String chainId) {
    if (chainId == null || chainId.isBlank()) {
      return Optional.empty();
    }

    ConfigSnapshot snapshot = currentSnapshot.get();
    return Optional.ofNullable(snapshot.ruleChainConfigs().get(chainId));
  }

  /**
   * 获取所有规则链配置
   */
  public List<RuleChainConfig> getAllRuleChainConfigs() {
    ConfigSnapshot snapshot = currentSnapshot.get();
    return new ArrayList<>(snapshot.ruleChainConfigs().values());
  }

  /**
   * 获取流程链配置
   */
  public Optional<ProcessChainConfig> getProcessChainConfig(String chainId) {
    if (chainId == null || chainId.isBlank()) {
      return Optional.empty();
    }

    ConfigSnapshot snapshot = currentSnapshot.get();
    return Optional.ofNullable(snapshot.processChainConfigs().get(chainId));
  }

  /**
   * 获取所有流程链配置
   */
  public List<ProcessChainConfig> getAllProcessChainConfigs() {
    ConfigSnapshot snapshot = currentSnapshot.get();
    return new ArrayList<>(snapshot.processChainConfigs().values());
  }

  // ============================= 领域对象访问 =============================

  /**
   * 获取规则链领域对象
   */
  public Optional<RuleChain> getRuleChain(String chainId) {
    if (chainId == null || chainId.isBlank()) {
      return Optional.empty();
    }

    ConfigSnapshot snapshot = currentSnapshot.get();
    return Optional.ofNullable(snapshot.ruleChains().get(chainId));
  }

  /**
   * 获取所有规则链领域对象
   */
  public List<RuleChain> getAllRuleChains() {
    ConfigSnapshot snapshot = currentSnapshot.get();
    return new ArrayList<>(snapshot.ruleChains().values());
  }

  /**
   * 获取流程链领域对象
   */
  public Optional<ProcessChain> getProcessChain(String chainId) {
    if (chainId == null || chainId.isBlank()) {
      return Optional.empty();
    }

    ConfigSnapshot snapshot = currentSnapshot.get();
    return Optional.ofNullable(snapshot.processChains().get(chainId));
  }

  /**
   * 获取所有流程链领域对象
   */
  public List<ProcessChain> getAllProcessChains() {
    ConfigSnapshot snapshot = currentSnapshot.get();
    return new ArrayList<>(snapshot.processChains().values());
  }

  // ============================= 缓存更新操作 =============================

  /**
   * 原子更新整个配置缓存
   *
   * @param newConfigSnapshot 新的配置快照
   */
  public void updateCache(ConfigSnapshot newConfigSnapshot) {
    lock.writeLock().lock();
    try {
      ConfigSnapshot oldSnapshot = currentSnapshot.get();
      currentSnapshot.set(newConfigSnapshot);

      // 更新统计信息
      statistics = new CacheStatistics(
        newConfigSnapshot.ruleChainConfigs().size(),
        newConfigSnapshot.processChainConfigs().size(),
        newConfigSnapshot.lastUpdateTime()
      );

      log.info("Cache updated successfully: ruleChains={}, processChains={}, oldTimestamp={}, newTimestamp={}",
        newConfigSnapshot.ruleChainConfigs().size(),
        newConfigSnapshot.processChainConfigs().size(),
        oldSnapshot.lastUpdateTime(),
        newConfigSnapshot.lastUpdateTime()
      );

    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * 增量更新规则链配置
   */
  public void updateRuleChainConfig(String chainId, RuleChainConfig config, RuleChain domainObject) {
    if (chainId == null || chainId.isBlank()) {
      return;
    }

    lock.writeLock().lock();
    try {
      ConfigSnapshot currentSnap = currentSnapshot.get();

      // 创建新的配置映射（不可变）
      Map<String, RuleChainConfig> newRuleChainConfigs = new HashMap<>(currentSnap.ruleChainConfigs());
      Map<String, RuleChain> newRuleChains = new HashMap<>(currentSnap.ruleChains());

      if (config != null) {
        newRuleChainConfigs.put(chainId, config);
      } else {
        newRuleChainConfigs.remove(chainId);
      }

      if (domainObject != null) {
        newRuleChains.put(chainId, domainObject);
      } else {
        newRuleChains.remove(chainId);
      }

      // 创建新的快照
      ConfigSnapshot newSnapshot = new ConfigSnapshot(
        Collections.unmodifiableMap(newRuleChainConfigs),
        currentSnap.processChainConfigs(),
        Collections.unmodifiableMap(newRuleChains),
        currentSnap.processChains(),
        System.currentTimeMillis()
      );

      currentSnapshot.set(newSnapshot);

      log.debug("Updated rule chain cache: {}", chainId);

    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * 增量更新流程链配置
   */
  public void updateProcessChainConfig(String chainId, ProcessChainConfig config, ProcessChain domainObject) {
    if (chainId == null || chainId.isBlank()) {
      return;
    }

    lock.writeLock().lock();
    try {
      ConfigSnapshot currentSnap = currentSnapshot.get();

      // 创建新的配置映射（不可变）
      Map<String, ProcessChainConfig> newProcessChainConfigs = new HashMap<>(currentSnap.processChainConfigs());
      Map<String, ProcessChain> newProcessChains = new HashMap<>(currentSnap.processChains());

      if (config != null) {
        newProcessChainConfigs.put(chainId, config);
      } else {
        newProcessChainConfigs.remove(chainId);
      }

      if (domainObject != null) {
        newProcessChains.put(chainId, domainObject);
      } else {
        newProcessChains.remove(chainId);
      }

      // 创建新的快照
      ConfigSnapshot newSnapshot = new ConfigSnapshot(
        currentSnap.ruleChainConfigs(),
        Collections.unmodifiableMap(newProcessChainConfigs),
        currentSnap.ruleChains(),
        Collections.unmodifiableMap(newProcessChains),
        System.currentTimeMillis()
      );

      currentSnapshot.set(newSnapshot);

      log.debug("Updated process chain cache: {}", chainId);

    } finally {
      lock.writeLock().unlock();
    }
  }

  // ============================= 缓存管理 =============================

  /**
   * 清空缓存
   */
  public void clearCache() {
    lock.writeLock().lock();
    try {
      currentSnapshot.set(new ConfigSnapshot(
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyMap(),
        System.currentTimeMillis()
      ));

      statistics = new CacheStatistics(0, 0, System.currentTimeMillis());

      log.info("Cache cleared successfully");

    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * 检查配置是否存在
   */
  public boolean existsRuleChain(String chainId) {
    return getRuleChainConfig(chainId).isPresent();
  }

  /**
   * 检查配置是否存在
   */
  public boolean existsProcessChain(String chainId) {
    return getProcessChainConfig(chainId).isPresent();
  }

  // ============================= 内部类 =============================

  /**
   * 不可变的配置快照
   * 保证缓存的线程安全性和一致性
   */
  public record ConfigSnapshot(
    Map<String, RuleChainConfig> ruleChainConfigs,
    Map<String, ProcessChainConfig> processChainConfigs,
    Map<String, RuleChain> ruleChains,
    Map<String, ProcessChain> processChains,
    long lastUpdateTime
  ) {
    public ConfigSnapshot {
      // 确保所有映射都是不可变的
      ruleChainConfigs = Map.copyOf(ruleChainConfigs);
      processChainConfigs = Map.copyOf(processChainConfigs);
      ruleChains = Map.copyOf(ruleChains);
      processChains = Map.copyOf(processChains);
    }
  }

  /**
   * 缓存统计信息
   */
  public record CacheStatistics(
    int ruleChainCount,
    int processChainCount,
    long lastUpdateTime
  ) {
  }
}
