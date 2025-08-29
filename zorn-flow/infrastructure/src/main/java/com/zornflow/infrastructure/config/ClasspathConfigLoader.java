package com.zornflow.infrastructure.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.zornflow.infrastructure.config.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ClasspathConfigLoader {

  private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();
  private final ConfigLocationProperties props;
  private final ResourcePatternResolver resolver;

  /* ========== 内存仓库（同前） ========== */
  private final Map<String, RuleConfig>        globalRules      = new ConcurrentHashMap<>();
  private final Map<String, ProcessNodeConfig> globalNodes      = new ConcurrentHashMap<>();
  private final Map<String, RuleChainConfig>   globalRuleChains = new ConcurrentHashMap<>();
  private final Map<String, ProcessChainConfig>   globalFlows      = new ConcurrentHashMap<>();

  public ClasspathConfigLoader(ResourcePatternResolver resolver, ConfigLocationProperties props) {
    this.props = props;
    this.resolver = resolver;
    loadAll();
  }

  private void loadAll() {
    try {
      loadGlobals("rules", globalRules, new TypeReference<>() {});
      loadGlobals("nodes", globalNodes, new TypeReference<>() {});
      loadRuleChains(props.getRuleChains());
      loadFlows(props.getFlows());
      log.info("Classpath config loaded: rules={}, nodes={}, ruleChains={}, flows={}",
        globalRules.size(), globalNodes.size(), globalRuleChains.size(), globalFlows.size());
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load classpath config", e);
    }
  }

  /* ---------- 通用加载模板 ---------- */
  private <T extends EngineModelConfigDTO> void loadGlobals(String folder,
                                                            Map<String, T> repo,
                                                            TypeReference<Map<String, T>> typeRef) throws IOException {
    Resource[] resources = resolver.getResources(props.getRoot() + folder + "/*.yml");
    for (Resource res : resources) {
      Map<String, T> map = mapper.readValue(res.getInputStream(), typeRef);
      for (var e : map.entrySet()) {
        if (repo.putIfAbsent(e.getKey(), e.getValue()) != null) {
          throw new IllegalStateException("Duplicate global key '" + e.getKey() + "' in " + res);
        }
      }
    }
  }

  /* ---------- 规则链 / 流程链加载（逻辑与之前相同，只需把 Path 换成 Resource） ---------- */
  private void loadRuleChains(String folder) throws IOException {
    Resource[] resources = resolver.getResources(props.getRoot() + folder + "/*.yml");
    for (Resource res : resources) {
      Map<String, RuleChainConfig> rawMap = mapper.readValue(res.getInputStream(), new TypeReference<>() {});
      for (var e : rawMap.entrySet()) {
        List<RuleConfig> resolved = new ArrayList<>();
        for (RuleConfig r : e.getValue().rules()) {
          resolved.add(resolveRule(r, res));
        }
        resolved.sort(Comparator.comparingInt(RuleConfig::priority));
        globalRuleChains.put(e.getKey(),
          new RuleChainConfig(e.getValue().id(), e.getValue().name(),
            e.getValue().version(), e.getValue().description(), resolved));
      }
    }
  }

  private void loadFlows(String folder) throws IOException {
    Resource[] resources = resolver.getResources(props.getRoot() + folder + "/*.yml");
    for (Resource res : resources) {
      Map<String, ProcessChainConfig> rawMap = mapper.readValue(res.getInputStream(), new TypeReference<>() {});
      for (var e : rawMap.entrySet()) {
        List<ProcessNodeConfig> resolved = new ArrayList<>();
        for (ProcessNodeConfig n : e.getValue().nodes()) {
          resolved.add(resolveNode(n, res));
        }
        globalFlows.put(e.getKey(),
          new ProcessChainConfig(e.getValue().id(), e.getValue().name(),
            e.getValue().version(), e.getValue().description(), resolved));
      }
    }
  }

  private RuleConfig resolveRule(RuleConfig yamlRuleConfig, Resource res) {
    RuleConfig base = globalRules.get(yamlRuleConfig.id());
    if (base == null) {
      globalRules.putIfAbsent(yamlRuleConfig.id(), yamlRuleConfig);
      return yamlRuleConfig;
    }
    return new RuleConfig(base.id(), base.name(),
      yamlRuleConfig.priority() != null ? yamlRuleConfig.priority() : base.priority(),
      yamlRuleConfig.condition() != null ? yamlRuleConfig.condition() : base.condition(),
      base.handle());
  }

  private ProcessNodeConfig resolveNode(ProcessNodeConfig yamlNode, Resource res) {
    ProcessNodeConfig base = globalNodes.get(yamlNode.id());
    if (base == null) {
      globalNodes.putIfAbsent(yamlNode.id(), yamlNode);
      return yamlNode;
    }
    return new ProcessNodeConfig(base.id(), base.name(), base.next(), base.type(),
      yamlNode.ruleChain() != null ? yamlNode.ruleChain() : base.ruleChain(),
      base.conditions(),
      yamlNode.properties() != null ? yamlNode.properties() : base.properties());
  }

  public Optional<RuleConfig>        rule(String id)        { return Optional.ofNullable(globalRules.get(id)); }
  public Optional<ProcessNodeConfig> node(String id)        { return Optional.ofNullable(globalNodes.get(id)); }
  public Optional<RuleChainConfig>   ruleChain(String id)  { return Optional.ofNullable(globalRuleChains.get(id)); }
  public Optional<ProcessChainConfig>   flow(String id)       { return Optional.ofNullable(globalFlows.get(id)); }
}
