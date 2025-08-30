package com.zornflow.infrastructure.config.dataaccess.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import com.zornflow.infrastructure.config.source.ReadableConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * YAML配置源实现
 * 从YAML文件中加载规则链和流程链配置
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29 10:10
 */
@Slf4j
public class YamlConfigSource implements ReadableConfigSource {

  final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
  final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
  // 使用ConfigurationProperties管理配置
  private final YamlConfigProperties yamlConfigProperties;
  // 缓存配置数据 - package-private for helper access
  volatile Map<String, RuleChainConfig> ruleChainConfigs = new HashMap<>();
  volatile Map<String, ProcessChainConfig> processChainConfigs = new HashMap<>();
  volatile Map<String, RuleConfig> globalRules = new HashMap<>();
  volatile Map<String, ProcessNodeConfig> globalNodes = new HashMap<>();
  private volatile boolean loaded = false;

  public YamlConfigSource(YamlConfigProperties yamlConfigProperties) {
    this.yamlConfigProperties = yamlConfigProperties;
  }

  @Override
  public String getSourceName() {
    return "YamlConfigSource";
  }

  @Override
  public ConfigSourceType getSourceType() {
    return ConfigSourceType.YAML;
  }

  public String getRuleChainsPath() {
    return yamlConfigProperties.getRuleChainsPath();
  }

  public String getProcessChainsPath() {
    return yamlConfigProperties.getProcessChainsPath();
  }

  public String getGlobalRulesPath() {
    return yamlConfigProperties.getGlobalRulesPath();
  }

  public String getGlobalNodesPath() {
    return yamlConfigProperties.getGlobalNodesPath();
  }

  public String getConfigBasePath() {
    return yamlConfigProperties.getBasePath();
  }

  @Override
  public synchronized Map<String, RuleChainConfig> loadRuleChainConfigs() {
    ensureLoaded();
    return new HashMap<>(ruleChainConfigs);
  }

  @Override
  public Optional<RuleChainConfig> loadRuleChainConfig(String ruleChainId) {
    ensureLoaded();
    return Optional.ofNullable(ruleChainConfigs.get(ruleChainId));
  }

  @Override
  public synchronized Map<String, ProcessChainConfig> loadProcessChainConfigs() {
    ensureLoaded();
    return new HashMap<>(processChainConfigs);
  }

  @Override
  public Optional<ProcessChainConfig> loadProcessChainConfig(String processChainId) {
    ensureLoaded();
    return Optional.ofNullable(processChainConfigs.get(processChainId));
  }

  @Override
  public synchronized Map<String, RuleConfig> loadGlobalRules() {
    ensureLoaded();
    return new HashMap<>(globalRules);
  }

  @Override
  public synchronized Map<String, ProcessNodeConfig> loadGlobalNodes() {
    ensureLoaded();
    return new HashMap<>(globalNodes);
  }

  @Override
  public boolean refresh() {
    try {
      synchronized (this) {
        loaded = false;
        ruleChainConfigs.clear();
        processChainConfigs.clear();
        globalRules.clear();
        globalNodes.clear();
        YamlConfigSourceHelper.loadAllConfigurations(this);
        loaded = true;
      }
      return true;
    } catch (Exception e) {
      log.error("刷新YAML配置源失败", e);
      return false;
    }
  }

  /**
   * 获取配置加载状态
   */
  private void ensureLoaded() {
    if (!loaded) {
      synchronized (this) {
        if (!loaded) {
          YamlConfigSourceHelper.loadAllConfigurations(this);
          loaded = true;
        }
      }
    }
  }
}
