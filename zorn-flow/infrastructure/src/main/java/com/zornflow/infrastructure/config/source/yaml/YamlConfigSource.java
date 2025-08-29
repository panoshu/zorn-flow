package com.zornflow.infrastructure.config.source.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.zornflow.infrastructure.config.model.*;
import com.zornflow.infrastructure.config.source.contract.ReadableConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类路径配置源实现
 * 从类路径(classpath)中的YAML文件加载配置信息
 * <p>
 * 特性：
 * - 只读配置源，不支持写入操作
 * - 支持公共配置和私有配置的合并
 * - 内存缓存配置以提高访问性能
 * - 支持配置热刷新（重新加载类路径资源）
 * <p>
 * 配置文件结构：
 * - rules/: 公共规则定义
 * - nodes/: 公共节点定义
 * - rule-chains/: 规则链定义
 * - flow-chains/: 流程链定义
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@Slf4j
@Component
public class YamlConfigSource implements ReadableConfigSource {

  private static final String SOURCE_NAME = "classpath";
  private static final int SOURCE_PRIORITY = 100; // 较低优先级，作为默认配置

  private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();
  private final YamlConfigProperties props;
  private final ResourcePatternResolver resolver;

  /* ========== 内存仓库 ========== */
  private final Map<String, RuleConfig> globalRules = new ConcurrentHashMap<>();
  private final Map<String, ProcessNodeConfig> globalNodes = new ConcurrentHashMap<>();
  private final Map<String, RuleChainConfig> globalRuleChains = new ConcurrentHashMap<>();
  private final Map<String, ProcessChainConfig> globalProcessChains = new ConcurrentHashMap<>();

  private volatile boolean initialized = false;

  public YamlConfigSource(ResourcePatternResolver resolver, YamlConfigProperties props) {
    this.props = props;
    this.resolver = resolver;
    initialize();
  }

  // ============================= ConfigSource 基本属性 =============================

  @Override
  public String getSourceName() {
    return SOURCE_NAME;
  }

  @Override
  public int getPriority() {
    return SOURCE_PRIORITY;
  }

  @Override
  public boolean isAvailable() {
    return initialized && !globalRuleChains.isEmpty() || !globalProcessChains.isEmpty();
  }

  @Override
  public ConfigSourceStatistics getStatistics() {
    return new ConfigSourceStatistics(
      SOURCE_NAME,
      globalRuleChains.size(),
      globalProcessChains.size(),
      isAvailable(),
      String.format("Rules: %d, Nodes: %d", globalRules.size(), globalNodes.size())
    );
  }

  // ============================= ReadableConfigSource 读取操作 =============================

  @Override
  public Optional<RuleChainConfig> findRuleChain(String chainId) {
    if (chainId == null || chainId.isBlank()) {
      return Optional.empty();
    }

    RuleChainConfig config = globalRuleChains.get(chainId);
    if (config != null) {
      log.debug("Found rule chain [{}] from classpath", chainId);
    }
    return Optional.ofNullable(config);
  }

  @Override
  public List<RuleChainConfig> findAllRuleChains() {
    List<RuleChainConfig> result = new ArrayList<>(globalRuleChains.values());
    log.debug("Loaded {} rule chains from classpath", result.size());
    return result;
  }

  @Override
  public Optional<ProcessChainConfig> findProcessChain(String chainId) {
    if (chainId == null || chainId.isBlank()) {
      return Optional.empty();
    }

    ProcessChainConfig config = globalProcessChains.get(chainId);
    if (config != null) {
      log.debug("Found process chain [{}] from classpath", chainId);
    }
    return Optional.ofNullable(config);
  }

  @Override
  public List<ProcessChainConfig> findAllProcessChains() {
    List<ProcessChainConfig> result = new ArrayList<>(globalProcessChains.values());
    log.debug("Loaded {} process chains from classpath", result.size());
    return result;
  }

  @Override
  public void refresh() {
    log.info("Refreshing classpath config source");
    try {
      // 清除现有缓存
      clearCache();
      // 重新加载
      initialize();
      log.info("Successfully refreshed classpath config source");
    } catch (Exception e) {
      log.error("Failed to refresh classpath config source", e);
      throw new RuntimeException("Failed to refresh classpath config source", e);
    }
  }

  // ============================= 兼容性方法（供现有代码调用） =============================

  /**
   * 根据ID获取规则配置（兼容性方法）
   *
   * @deprecated 使用 findRuleChain 替代
   */
  @Deprecated
  public Optional<RuleConfig> rule(String id) {
    return Optional.ofNullable(globalRules.get(id));
  }

  /**
   * 根据ID获取节点配置（兼容性方法）
   *
   * @deprecated 使用 findProcessChain 替代
   */
  @Deprecated
  public Optional<ProcessNodeConfig> node(String id) {
    return Optional.ofNullable(globalNodes.get(id));
  }

  /**
   * 根据ID获取规则链配置（兼容性方法）
   *
   * @deprecated 使用 findRuleChain 替代
   */
  @Deprecated
  public Optional<RuleChainConfig> ruleChain(String id) {
    return findRuleChain(id);
  }

  /**
   * 根据ID获取流程链配置（兼容性方法）
   *
   * @deprecated 使用 findProcessChain 替代
   */
  @Deprecated
  public Optional<ProcessChainConfig> flow(String id) {
    return findProcessChain(id);
  }

  // ============================= 私有方法 =============================

  /**
   * 初始化配置加载
   */
  private void initialize() {
    try {
      loadGlobals("rules", globalRules, new TypeReference<>() {
      });
      loadGlobals("nodes", globalNodes, new TypeReference<>() {
      });
      loadRuleChains(props.getRuleChains());
      loadProcessChains(props.getFlows());

      initialized = true;

      log.info("Classpath config loaded: rules={}, nodes={}, ruleChains={}, processChains={}",
        globalRules.size(), globalNodes.size(), globalRuleChains.size(), globalProcessChains.size());

    } catch (IOException e) {
      initialized = false;
      throw new UncheckedIOException("Failed to load classpath config", e);
    }
  }

  /**
   * 清除缓存
   */
  private void clearCache() {
    globalRules.clear();
    globalNodes.clear();
    globalRuleChains.clear();
    globalProcessChains.clear();
    initialized = false;
  }

  /**
   * 加载全局配置（规则或节点）
   */
  private <T extends EngineModelConfigDTO> void loadGlobals(String folder,
                                                            Map<String, T> repo,
                                                            TypeReference<Map<String, T>> typeRef) throws IOException {
    Resource[] resources = resolver.getResources(props.getRoot() + folder + "/*.yml");
    for (Resource res : resources) {
      Map<String, T> map = mapper.readValue(res.getInputStream(), typeRef);
      for (var entry : map.entrySet()) {
        if (repo.putIfAbsent(entry.getKey(), entry.getValue()) != null) {
          throw new IllegalStateException("Duplicate global key '" + entry.getKey() + "' in " + res);
        }
      }
    }
  }

  /**
   * 加载规则链配置
   */
  private void loadRuleChains(String folder) throws IOException {
    Resource[] resources = resolver.getResources(props.getRoot() + folder + "/*.yml");
    for (Resource res : resources) {
      Map<String, RuleChainConfig> rawMap = mapper.readValue(res.getInputStream(), new TypeReference<>() {
      });
      for (var entry : rawMap.entrySet()) {
        List<RuleConfig> resolved = new ArrayList<>();
        for (RuleConfig rule : entry.getValue().rules()) {
          resolved.add(resolveRule(rule, res));
        }
        resolved.sort(Comparator.comparingInt(RuleConfig::priority));

        RuleChainConfig resolvedChain = new RuleChainConfig(
          entry.getValue().id(),
          entry.getValue().name(),
          entry.getValue().version(),
          entry.getValue().description(),
          resolved
        );
        globalRuleChains.put(entry.getKey(), resolvedChain);
      }
    }
  }

  /**
   * 加载流程链配置
   */
  private void loadProcessChains(String folder) throws IOException {
    Resource[] resources = resolver.getResources(props.getRoot() + folder + "/*.yml");
    for (Resource res : resources) {
      Map<String, ProcessChainConfig> rawMap = mapper.readValue(res.getInputStream(), new TypeReference<>() {
      });
      for (var entry : rawMap.entrySet()) {
        List<ProcessNodeConfig> resolved = new ArrayList<>();
        for (ProcessNodeConfig node : entry.getValue().nodes()) {
          resolved.add(resolveNode(node, res));
        }

        ProcessChainConfig resolvedChain = new ProcessChainConfig(
          entry.getValue().id(),
          entry.getValue().name(),
          entry.getValue().version(),
          entry.getValue().description(),
          resolved
        );
        globalProcessChains.put(entry.getKey(), resolvedChain);
      }
    }
  }

  /**
   * 解析规则配置，合并公共规则和私有规则
   */
  private RuleConfig resolveRule(RuleConfig yamlRuleConfig, Resource res) {
    RuleConfig base = globalRules.get(yamlRuleConfig.id());
    if (base == null) {
      globalRules.putIfAbsent(yamlRuleConfig.id(), yamlRuleConfig);
      return yamlRuleConfig;
    }
    return new RuleConfig(
      base.id(),
      base.name(),
      yamlRuleConfig.priority() != null ? yamlRuleConfig.priority() : base.priority(),
      yamlRuleConfig.condition() != null ? yamlRuleConfig.condition() : base.condition(),
      base.handle()
    );
  }

  /**
   * 解析节点配置，合并公共节点和私有节点
   */
  private ProcessNodeConfig resolveNode(ProcessNodeConfig yamlNode, Resource res) {
    ProcessNodeConfig base = globalNodes.get(yamlNode.id());
    if (base == null) {
      globalNodes.putIfAbsent(yamlNode.id(), yamlNode);
      return yamlNode;
    }
    return new ProcessNodeConfig(
      base.id(),
      base.name(),
      base.next(),
      base.type(),
      yamlNode.ruleChain() != null ? yamlNode.ruleChain() : base.ruleChain(),
      base.conditions(),
      yamlNode.properties() != null ? yamlNode.properties() : base.properties()
    );
  }
}
