package com.zornflow.infrastructure.config.source;

import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.source.contract.ConfigSource;
import com.zornflow.infrastructure.config.source.contract.ReadableConfigSource;
import com.zornflow.infrastructure.config.source.contract.WritableConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 配置源管理器
 * 负责管理多个配置源，实现配置合并和优先级策略
 * <p>
 * 设计模式：
 * - 策略模式：支持不同的配置合并策略
 * - 组合模式：将多个配置源组合成统一的访问接口
 * - 观察者模式：支持配置变更通知（可扩展）
 * <p>
 * 设计原则：
 * - 单一职责：专注于配置源的管理和协调
 * - 开闭原则：新增配置源不需要修改现有代码
 * - 依赖倒置：依赖配置源抽象而非具体实现
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@Slf4j
@Component
public class ConfigSourceManager {

  private final List<ReadableConfigSource> readableSources = new ArrayList<>();
  private final List<WritableConfigSource> writableSources = new ArrayList<>();
  private final Map<String, ReadableConfigSource> sourceNameMap = new ConcurrentHashMap<>();

  /**
   * 注册可读配置源
   *
   * @param source 可读配置源
   */
  public void registerReadableSource(ReadableConfigSource source) {
    if (source == null) {
      log.warn("Attempted to register null readable config source");
      return;
    }

    String sourceName = source.getSourceName();
    if (sourceNameMap.containsKey(sourceName)) {
      log.warn("Config source with name '{}' already registered, ignoring", sourceName);
      return;
    }

    readableSources.add(source);
    sourceNameMap.put(sourceName, source);

    // 按优先级排序（优先级数值越小越优先）
    readableSources.sort(Comparator.comparingInt(ReadableConfigSource::getPriority));

    log.info("Registered readable config source: {} (priority: {})",
      sourceName, source.getPriority());
  }

  /**
   * 注册可写配置源
   *
   * @param source 可写配置源
   */
  public void registerWritableSource(WritableConfigSource source) {
    if (source == null) {
      log.warn("Attempted to register null writable config source");
      return;
    }

    registerReadableSource(source); // 可写配置源也是可读的
    writableSources.add(source);
    writableSources.sort(Comparator.comparingInt(WritableConfigSource::getPriority));

    log.info("Registered writable config source: {} (priority: {})",
      source.getSourceName(), source.getPriority());
  }

  // ============================= 读取操作 =============================

  /**
   * 根据ID查找规则链配置
   * 按优先级遍历所有可用的配置源，返回第一个找到的配置
   *
   * @param chainId 规则链ID
   * @return 规则链配置的Optional包装
   */
  public Optional<RuleChainConfig> findRuleChain(String chainId) {
    if (chainId == null || chainId.isBlank()) {
      log.debug("Rule chain ID is null or blank");
      return Optional.empty();
    }

    log.debug("Finding rule chain from config sources: {}", chainId);

    for (ReadableConfigSource source : getAvailableReadableSources()) {
      try {
        Optional<RuleChainConfig> result = source.findRuleChain(chainId);
        if (result.isPresent()) {
          log.debug("Found rule chain [{}] from source: {}", chainId, source.getSourceName());
          return result;
        }
      } catch (Exception e) {
        log.warn("Failed to find rule chain [{}] from source: {}",
          chainId, source.getSourceName(), e);
      }
    }

    log.debug("Rule chain [{}] not found in any config source", chainId);
    return Optional.empty();
  }

  /**
   * 获取所有规则链配置
   * 合并所有配置源的配置，优先级高的配置源会覆盖优先级低的同名配置
   *
   * @return 合并后的规则链配置列表
   */
  public List<RuleChainConfig> findAllRuleChains() {
    log.debug("Finding all rule chains from config sources");

    Map<String, RuleChainConfig> mergedConfigs = new LinkedHashMap<>();

    // 按优先级从低到高遍历（后加入的会覆盖先加入的）
    List<ReadableConfigSource> reversedSources = new ArrayList<>(getAvailableReadableSources());
    Collections.reverse(reversedSources);

    for (ReadableConfigSource source : reversedSources) {
      try {
        List<RuleChainConfig> configs = source.findAllRuleChains();
        for (RuleChainConfig config : configs) {
          mergedConfigs.put(config.id(), config);
        }
        log.debug("Loaded {} rule chains from source: {}",
          configs.size(), source.getSourceName());
      } catch (Exception e) {
        log.warn("Failed to load rule chains from source: {}",
          source.getSourceName(), e);
      }
    }

    List<RuleChainConfig> result = new ArrayList<>(mergedConfigs.values());
    log.info("Merged {} rule chains from {} config sources",
      result.size(), getAvailableReadableSources().size());
    return result;
  }

  /**
   * 根据ID查找流程链配置
   *
   * @param chainId 流程链ID
   * @return 流程链配置的Optional包装
   */
  public Optional<ProcessChainConfig> findProcessChain(String chainId) {
    if (chainId == null || chainId.isBlank()) {
      log.debug("Process chain ID is null or blank");
      return Optional.empty();
    }

    log.debug("Finding process chain from config sources: {}", chainId);

    for (ReadableConfigSource source : getAvailableReadableSources()) {
      try {
        Optional<ProcessChainConfig> result = source.findProcessChain(chainId);
        if (result.isPresent()) {
          log.debug("Found process chain [{}] from source: {}", chainId, source.getSourceName());
          return result;
        }
      } catch (Exception e) {
        log.warn("Failed to find process chain [{}] from source: {}",
          chainId, source.getSourceName(), e);
      }
    }

    log.debug("Process chain [{}] not found in any config source", chainId);
    return Optional.empty();
  }

  /**
   * 获取所有流程链配置
   *
   * @return 合并后的流程链配置列表
   */
  public List<ProcessChainConfig> findAllProcessChains() {
    log.debug("Finding all process chains from config sources");

    Map<String, ProcessChainConfig> mergedConfigs = new LinkedHashMap<>();

    // 按优先级从低到高遍历
    List<ReadableConfigSource> reversedSources = new ArrayList<>(getAvailableReadableSources());
    Collections.reverse(reversedSources);

    for (ReadableConfigSource source : reversedSources) {
      try {
        List<ProcessChainConfig> configs = source.findAllProcessChains();
        for (ProcessChainConfig config : configs) {
          mergedConfigs.put(config.id(), config);
        }
        log.debug("Loaded {} process chains from source: {}",
          configs.size(), source.getSourceName());
      } catch (Exception e) {
        log.warn("Failed to load process chains from source: {}",
          source.getSourceName(), e);
      }
    }

    List<ProcessChainConfig> result = new ArrayList<>(mergedConfigs.values());
    log.info("Merged {} process chains from {} config sources",
      result.size(), getAvailableReadableSources().size());
    return result;
  }

  // ============================= 写入操作 =============================

  /**
   * 保存规则链配置
   * 策略：先查找配置所在的可写配置源，如果找到则在该源执行操作；否则选择最高优先级的可写配置源
   *
   * @param config 规则链配置
   * @return 操作结果
   */
  public WritableConfigSource.ConfigOperationResult saveRuleChain(RuleChainConfig config) {
    if (config == null || config.id() == null) {
      return new WritableConfigSource.ConfigOperationResult(
        false, "Rule chain config or id is null", null);
    }

    // 1. 先查找配置所在的可写配置源
    WritableConfigSource targetSource = findWritableSourceContaining(config.id(), true);

    // 2. 如果没找到，选择最高优先级的可写配置源作为兜底
    if (targetSource == null) {
      targetSource = getHighestPriorityWritableSource();
    }

    if (targetSource == null) {
      return new WritableConfigSource.ConfigOperationResult(
        false, "No writable config source available", config.id());
    }

    log.debug("Saving rule chain [{}] to source: {}", config.id(), targetSource.getSourceName());
    return targetSource.saveRuleChain(config);
  }

  /**
   * 保存流程链配置
   * 策略：先查找配置所在的可写配置源，如果找到则在该源执行操作；否则选择最高优先级的可写配置源
   *
   * @param config 流程链配置
   * @return 操作结果
   */
  public WritableConfigSource.ConfigOperationResult saveProcessChain(ProcessChainConfig config) {
    if (config == null || config.id() == null) {
      return new WritableConfigSource.ConfigOperationResult(
        false, "Process chain config or id is null", null);
    }

    // 1. 先查找配置所在的可写配置源
    WritableConfigSource targetSource = findWritableSourceContaining(config.id(), false);

    // 2. 如果没找到，选择最高优先级的可写配置源作为兜底
    if (targetSource == null) {
      targetSource = getHighestPriorityWritableSource();
    }

    if (targetSource == null) {
      return new WritableConfigSource.ConfigOperationResult(
        false, "No writable config source available", config.id());
    }

    log.debug("Saving process chain [{}] to source: {}", config.id(), targetSource.getSourceName());
    return targetSource.saveProcessChain(config);
  }

  /**
   * 删除规则链配置
   * 策略：查找配置所在的可写配置源并执行删除操作
   *
   * @param chainId 规则链ID
   * @return 操作结果
   */
  public WritableConfigSource.ConfigOperationResult deleteRuleChain(String chainId) {
    if (chainId == null || chainId.isBlank()) {
      return new WritableConfigSource.ConfigOperationResult(
        false, "Rule chain ID is null or blank", chainId);
    }

    // 查找配置所在的可写配置源
    WritableConfigSource targetSource = findWritableSourceContaining(chainId, true);

    if (targetSource == null) {
      return new WritableConfigSource.ConfigOperationResult(
        false, "Rule chain not found in any writable config source", chainId);
    }

    log.debug("Deleting rule chain [{}] from source: {}", chainId, targetSource.getSourceName());
    return targetSource.deleteRuleChain(chainId);
  }

  /**
   * 删除流程链配置
   * 策略：查找配置所在的可写配置源并执行删除操作
   *
   * @param chainId 流程链ID
   * @return 操作结果
   */
  public WritableConfigSource.ConfigOperationResult deleteProcessChain(String chainId) {
    if (chainId == null || chainId.isBlank()) {
      return new WritableConfigSource.ConfigOperationResult(
        false, "Process chain ID is null or blank", chainId);
    }

    // 查找配置所在的可写配置源
    WritableConfigSource targetSource = findWritableSourceContaining(chainId, false);

    if (targetSource == null) {
      return new WritableConfigSource.ConfigOperationResult(
        false, "Process chain not found in any writable config source", chainId);
    }

    log.debug("Deleting process chain [{}] from source: {}", chainId, targetSource.getSourceName());
    return targetSource.deleteProcessChain(chainId);
  }

  // ============================= 管理操作 =============================

  /**
   * 刷新所有配置源
   */
  public void refreshAll() {
    log.info("Refreshing all config sources");

    for (ReadableConfigSource source : readableSources) {
      try {
        source.refresh();
        log.debug("Refreshed config source: {}", source.getSourceName());
      } catch (Exception e) {
        log.warn("Failed to refresh config source: {}", source.getSourceName(), e);
      }
    }
  }

  /**
   * 获取配置源统计信息
   *
   * @return 统计信息列表
   */
  public List<ConfigSource.ConfigSourceStatistics> getStatistics() {
    return readableSources.stream()
      .map(source -> {
        try {
          return source.getStatistics();
        } catch (Exception e) {
          log.warn("Failed to get statistics from source: {}", source.getSourceName(), e);
          return new ConfigSource.ConfigSourceStatistics(
            source.getSourceName(), 0, 0, false, "Statistics unavailable");
        }
      })
      .collect(Collectors.toList());
  }

  /**
   * 根据名称获取配置源
   *
   * @param sourceName 配置源名称
   * @return 配置源的Optional包装
   */
  public Optional<ReadableConfigSource> getSourceByName(String sourceName) {
    return Optional.ofNullable(sourceNameMap.get(sourceName));
  }

  // ============================= 辅助方法 =============================

  /**
   * 获取所有可用的可读配置源
   */
  private List<ReadableConfigSource> getAvailableReadableSources() {
    return readableSources.stream()
      .filter(source -> {
        try {
          return source.isAvailable();
        } catch (Exception e) {
          log.warn("Failed to check availability of source: {}", source.getSourceName(), e);
          return false;
        }
      })
      .toList();
  }

  /**
   * 获取首个可用的可写配置源
   */
  private WritableConfigSource getFirstAvailableWritableSource() {
    return writableSources.stream()
      .filter(source -> {
        try {
          return source.isAvailable();
        } catch (Exception e) {
          log.warn("Failed to check availability of writable source: {}",
            source.getSourceName(), e);
          return false;
        }
      })
      .findFirst()
      .orElse(null);
  }

  /**
   * 查找包含指定配置的可写配置源
   *
   * @param chainId     配置链ID
   * @param isRuleChain 是否为规则链，true表示规则链，false表示流程链
   * @return 包含该配置的可写配置源，如果没找到返回null
   */
  private WritableConfigSource findWritableSourceContaining(String chainId, boolean isRuleChain) {
    if (chainId == null || chainId.isBlank()) {
      return null;
    }

    for (WritableConfigSource source : writableSources) {
      try {
        if (!source.isAvailable()) {
          continue;
        }

        boolean exists = isRuleChain
          ? source.findRuleChain(chainId).isPresent()
          : source.findProcessChain(chainId).isPresent();

        if (exists) {
          log.debug("Found {} chain [{}] in writable source: {}",
            isRuleChain ? "rule" : "process", chainId, source.getSourceName());
          return source;
        }
      } catch (Exception e) {
        log.warn("Failed to check {} chain [{}] in source: {}",
          isRuleChain ? "rule" : "process", chainId, source.getSourceName(), e);
      }
    }

    log.debug("{} chain [{}] not found in any writable source",
      isRuleChain ? "Rule" : "Process", chainId);
    return null;
  }

  /**
   * 获取最高优先级的可写配置源
   *
   * @return 优先级最高的可写配置源，如果没有可用的返回null
   */
  private WritableConfigSource getHighestPriorityWritableSource() {
    return writableSources.stream()
      .filter(source -> {
        try {
          return source.isAvailable();
        } catch (Exception e) {
          log.warn("Failed to check availability of writable source: {}",
            source.getSourceName(), e);
          return false;
        }
      })
      .min(Comparator.comparingInt(WritableConfigSource::getPriority)) // 数值越小优先级越高
      .orElse(null);
  }
}
