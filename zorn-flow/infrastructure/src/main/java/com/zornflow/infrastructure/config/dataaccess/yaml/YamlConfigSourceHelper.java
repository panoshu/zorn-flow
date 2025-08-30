package com.zornflow.infrastructure.config.dataaccess.yaml;

import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * YamlConfigSource的配置处理逻辑
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29 11:00
 */
@Slf4j
public class YamlConfigSourceHelper {

  /**
   * 加载所有配置
   */
  public static void loadAllConfigurations(YamlConfigSource source) {
    try {
      // 1. 首先加载全局规则和节点
      loadGlobalRulesFromResources(source);
      loadGlobalNodesFromResources(source);

      // 2. 然后加载规则链，支持引用全局规则
      loadRuleChainsFromResources(source);

      // 3. 最后加载流程链，支持引用全局节点
      loadProcessChainsFromResources(source);

      log.info("YAML配置加载完成 - 规则链: {}, 流程链: {}, 全局规则: {}, 全局节点: {}",
        source.ruleChainConfigs.size(), source.processChainConfigs.size(),
        source.globalRules.size(), source.globalNodes.size());
    } catch (Exception e) {
      log.error("加载YAML配置失败", e);
      throw new RuntimeException("加载YAML配置失败", e);
    }
  }

  /**
   * 加载全局规则
   */
  private static void loadGlobalRulesFromResources(YamlConfigSource source) throws IOException {
    String globalRulesPath = source.getGlobalRulesPath(); // 使用新的getter方法
    Resource[] resources = source.resourceResolver.getResources(globalRulesPath);
    log.debug("找到全局规则文件 {} 个", resources.length);

    for (Resource resource : resources) {
      if (resource.exists() && resource.isReadable()) {
        try (InputStream inputStream = resource.getInputStream()) {
          @SuppressWarnings("unchecked")
          Map<String, Object> yamlContent = source.yamlMapper.readValue(inputStream, Map.class);

          for (Map.Entry<String, Object> entry : yamlContent.entrySet()) {
            String ruleId = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> ruleData = (Map<String, Object>) entry.getValue();

            RuleConfig ruleConfig = convertToRuleConfig(ruleData);
            source.globalRules.put(ruleId, ruleConfig);
            log.debug("加载全局规则: {} from {}", ruleId, resource.getFilename());
          }
        }
      }
    }
  }

  /**
   * 加载全局节点
   */
  private static void loadGlobalNodesFromResources(YamlConfigSource source) throws IOException {
    String globalNodesPath = source.getGlobalNodesPath(); // 使用新的getter方法
    Resource[] resources = source.resourceResolver.getResources(globalNodesPath);
    log.debug("找到全局节点文件 {} 个", resources.length);

    for (Resource resource : resources) {
      if (resource.exists() && resource.isReadable()) {
        try (InputStream inputStream = resource.getInputStream()) {
          @SuppressWarnings("unchecked")
          Map<String, Object> yamlContent = source.yamlMapper.readValue(inputStream, Map.class);

          for (Map.Entry<String, Object> entry : yamlContent.entrySet()) {
            String nodeId = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> nodeData = (Map<String, Object>) entry.getValue();

            ProcessNodeConfig nodeConfig = convertToProcessNodeConfig(nodeData);
            source.globalNodes.put(nodeId, nodeConfig);
            log.debug("加载全局节点: {} from {}", nodeId, resource.getFilename());
          }
        }
      }
    }
  }

  /**
   * 加载规则链
   */
  private static void loadRuleChainsFromResources(YamlConfigSource source) throws IOException {
    String ruleChainsPath = source.getRuleChainsPath(); // 使用新的getter方法
    Resource[] resources = source.resourceResolver.getResources(ruleChainsPath);
    log.debug("找到规则链文件 {} 个", resources.length);

    for (Resource resource : resources) {
      if (resource.exists() && resource.isReadable()) {
        try (InputStream inputStream = resource.getInputStream()) {
          @SuppressWarnings("unchecked")
          Map<String, Object> yamlContent = source.yamlMapper.readValue(inputStream, Map.class);

          for (Map.Entry<String, Object> entry : yamlContent.entrySet()) {
            String chainId = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> chainData = (Map<String, Object>) entry.getValue();

            RuleChainConfig chainConfig = convertToRuleChainConfig(chainData, source);
            source.ruleChainConfigs.put(chainId, chainConfig);
            log.debug("加载规则链: {} from {}", chainId, resource.getFilename());
          }
        }
      }
    }
  }

  /**
   * 加载流程链
   */
  private static void loadProcessChainsFromResources(YamlConfigSource source) throws IOException {
    String processChainsPath = source.getProcessChainsPath(); // 使用新的getter方法
    Resource[] resources = source.resourceResolver.getResources(processChainsPath);
    log.debug("找到流程链文件 {} 个", resources.length);

    for (Resource resource : resources) {
      if (resource.exists() && resource.isReadable()) {
        try (InputStream inputStream = resource.getInputStream()) {
          @SuppressWarnings("unchecked")
          Map<String, Object> yamlContent = source.yamlMapper.readValue(inputStream, Map.class);

          for (Map.Entry<String, Object> entry : yamlContent.entrySet()) {
            String chainId = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> chainData = (Map<String, Object>) entry.getValue();

            ProcessChainConfig chainConfig = convertToProcessChainConfig(chainData, source);
            source.processChainConfigs.put(chainId, chainConfig);
            log.debug("加载流程链: {} from {}", chainId, resource.getFilename());
          }
        }
      }
    }
  }

  /**
   * 转换Map为RuleChainConfig
   */
  @SuppressWarnings("unchecked")
  private static RuleChainConfig convertToRuleChainConfig(Map<String, Object> data, YamlConfigSource source) {
    RuleChainConfig.RuleChainConfigBuilder builder = RuleChainConfig.builder();

    builder.id((String) data.get("id"));
    builder.name((String) data.get("name"));
    builder.version((String) data.get("version"));
    builder.description((String) data.get("description"));

    // 处理规则列表
    List<RuleConfig> rules = new ArrayList<>();
    Object rulesData = data.get("rules");
    if (rulesData instanceof List) {
      List<Map<String, Object>> rulesList = (List<Map<String, Object>>) rulesData;
      for (Map<String, Object> ruleData : rulesList) {
        RuleConfig ruleConfig = mergeWithGlobalRule(ruleData, source);
        rules.add(ruleConfig);
      }
    }
    builder.rules(rules);

    return builder.build();
  }

  /**
   * 与全局规则合并，支持局部覆盖
   */
  private static RuleConfig mergeWithGlobalRule(Map<String, Object> localRuleData, YamlConfigSource source) {
    String ruleId = (String) localRuleData.get("id");

    // 获取全局规则作为基础
    RuleConfig globalRule = source.globalRules.get(ruleId);

    if (globalRule != null) {
      // 合并全局规则与局部配置
      return RuleConfig.builder()
        .id(ruleId)
        .name(getStringValue(localRuleData, "name", globalRule.name()))
        .priority(getIntegerValue(localRuleData, "priority", globalRule.priority()))
        .condition(getStringValue(localRuleData, "condition", globalRule.condition()))
        .handle(getHandlerValue(localRuleData, "handle", globalRule.handle()))
        .build();
    } else {
      // 纯局部规则
      return convertToRuleConfig(localRuleData);
    }
  }

  /**
   * 转换Map为RuleConfig
   */
  @SuppressWarnings("unchecked")
  private static RuleConfig convertToRuleConfig(Map<String, Object> data) {
    RuleConfig.RuleConfigBuilder builder = RuleConfig.builder();

    builder.id((String) data.get("id"));
    builder.name((String) data.get("name"));
    builder.priority(getIntegerValue(data, "priority", 100));
    builder.condition((String) data.get("condition"));

    // 处理handler
    Object handleData = data.get("handle");
    if (handleData instanceof Map) {
      Map<String, Object> handleMap = (Map<String, Object>) handleData;
      RuleConfig.Handler.Type type = RuleConfig.Handler.Type.valueOf((String) handleMap.get("type"));
      String handler = (String) handleMap.get("handler");
      @SuppressWarnings("unchecked")
      Map<String, Object> parameters = (Map<String, Object>) handleMap.get("parameters");

      RuleConfig.Handler handlerObj = new RuleConfig.Handler(type, handler, parameters);
      builder.handle(handlerObj);
    }

    return builder.build();
  }

  /**
   * 转换Map为ProcessChainConfig
   */
  @SuppressWarnings("unchecked")
  private static ProcessChainConfig convertToProcessChainConfig(Map<String, Object> data, YamlConfigSource source) {
    ProcessChainConfig.ProcessChainConfigBuilder builder = ProcessChainConfig.builder();

    builder.id((String) data.get("id"));
    builder.name((String) data.get("name"));
    builder.version((String) data.get("version"));
    builder.description((String) data.get("description"));

    // 处理节点列表
    List<ProcessNodeConfig> nodes = new ArrayList<>();
    Object nodesData = data.get("nodes");
    if (nodesData instanceof List) {
      List<Map<String, Object>> nodesList = (List<Map<String, Object>>) nodesData;
      for (Map<String, Object> nodeData : nodesList) {
        ProcessNodeConfig nodeConfig = mergeWithGlobalNode(nodeData, source);
        nodes.add(nodeConfig);
      }
    }
    builder.nodes(nodes);

    return builder.build();
  }

  /**
   * 与全局节点合并，支持局部覆盖
   */
  private static ProcessNodeConfig mergeWithGlobalNode(Map<String, Object> localNodeData, YamlConfigSource source) {
    String nodeId = (String) localNodeData.get("id");

    // 获取全局节点作为基础
    ProcessNodeConfig globalNode = source.globalNodes.get(nodeId);

    if (globalNode != null) {
      // 合并全局节点与局部配置
      return ProcessNodeConfig.builder()
        .id(nodeId)
        .name(getStringValue(localNodeData, "name", globalNode.name()))
        .next(getStringValue(localNodeData, "next", globalNode.next()))
        .type(getNodeTypeValue(localNodeData, "type", globalNode.type()))
        .ruleChain(getStringValue(localNodeData, "ruleChain", globalNode.ruleChain()))
        .conditions(getConditionsValue(localNodeData, "conditions", globalNode.conditions()))
        .properties(getPropertiesValue(localNodeData, "properties", globalNode.properties()))
        .build();
    } else {
      // 纯局部节点
      return convertToProcessNodeConfig(localNodeData);
    }
  }

  /**
   * 转换Map为ProcessNodeConfig
   */
  @SuppressWarnings("unchecked")
  private static ProcessNodeConfig convertToProcessNodeConfig(Map<String, Object> data) {
    ProcessNodeConfig.ProcessNodeConfigBuilder builder = ProcessNodeConfig.builder();

    builder.id((String) data.get("id"));
    builder.name((String) data.get("name"));
    builder.next((String) data.get("next"));

    String typeStr = (String) data.get("type");
    if (typeStr != null) {
      builder.type(ProcessNodeConfig.NodeType.valueOf(typeStr));
    }

    builder.ruleChain((String) data.get("ruleChain"));
    builder.conditions(getConditionsValue(data, "conditions", null));
    builder.properties(getPropertiesValue(data, "properties", null));

    return builder.build();
  }

  // 辅助方法
  private static String getStringValue(Map<String, Object> data, String key, String defaultValue) {
    Object value = data.get(key);
    return value != null ? (String) value : defaultValue;
  }

  private static Integer getIntegerValue(Map<String, Object> data, String key, Integer defaultValue) {
    Object value = data.get(key);
    return value != null ? (Integer) value : defaultValue;
  }

  private static RuleConfig.Handler getHandlerValue(Map<String, Object> data, String key, RuleConfig.Handler defaultValue) {
    Object value = data.get(key);
    return value != null ? convertToHandler((Map<String, Object>) value) : defaultValue;
  }

  @SuppressWarnings("unchecked")
  private static RuleConfig.Handler convertToHandler(Map<String, Object> handleMap) {
    RuleConfig.Handler.Type type = RuleConfig.Handler.Type.valueOf((String) handleMap.get("type"));
    String handler = (String) handleMap.get("handler");
    Map<String, Object> parameters = (Map<String, Object>) handleMap.get("parameters");
    return new RuleConfig.Handler(type, handler, parameters);
  }

  private static ProcessNodeConfig.NodeType getNodeTypeValue(Map<String, Object> data, String key, ProcessNodeConfig.NodeType defaultValue) {
    Object value = data.get(key);
    return value != null ? ProcessNodeConfig.NodeType.valueOf((String) value) : defaultValue;
  }

  @SuppressWarnings("unchecked")
  private static List<com.zornflow.infrastructure.config.model.GatewayConditionConfig> getConditionsValue(Map<String, Object> data, String key, List<com.zornflow.infrastructure.config.model.GatewayConditionConfig> defaultValue) {
    Object value = data.get(key);
    if (value instanceof List) {
      // 简化处理，实际项目中需要完整实现
      return new ArrayList<>();
    }
    return defaultValue;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getPropertiesValue(Map<String, Object> data, String key, Map<String, Object> defaultValue) {
    Object value = data.get(key);
    if (value instanceof Map) {
      Map<String, Object> properties = new HashMap<>((Map<String, Object>) value);
      if (defaultValue != null) {
        // 合并属性：局部覆盖全局
        Map<String, Object> merged = new HashMap<>(defaultValue);
        merged.putAll(properties);
        return merged;
      }
      return properties;
    }
    return defaultValue != null ? new HashMap<>(defaultValue) : new HashMap<>();
  }
}
