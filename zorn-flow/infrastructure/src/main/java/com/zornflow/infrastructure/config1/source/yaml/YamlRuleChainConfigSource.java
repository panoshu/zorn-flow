package com.zornflow.infrastructure.config1.source.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.zornflow.domain.common.config.source.ReadableConfigSource;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 4:00
 */

@Slf4j
@RequiredArgsConstructor
public class YamlRuleChainConfigSource implements ReadableConfigSource<RuleChainConfig> {

  private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
  private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
  private final YamlRuleChainProperties yamlRuleChainProperties;

  volatile Map<String, RuleConfig> sharedRules = new HashMap<>();

  @Override
  public Optional<RuleChainConfig> load(String id) throws IOException {
    return Optional.ofNullable(getRuleChains().get(id));
  }

  @Override
  public Map<String, RuleChainConfig> loadAll() throws IOException {
    return getRuleChains();
  }

  @Override
  public String getSourceName() {
    return "YamlRuleChainConfigSource";
  }

  @Override
  public ConfigSourceType getSourceType() {
    return ConfigSourceType.YAML;
  }

  @Override
  public boolean available() {
    return false;
  }

  private Map<String, RuleConfig> getSharedRules() throws IOException {
    Resource[] resources = this.resourceResolver.getResources(this.yamlRuleChainProperties.getSharedRulesPath());
    log.debug("找到全局规则文件 {} 个", resources.length);

    // var sharedRules = new HashMap<String, RuleConfig>();

    for (Resource resource : resources){
      if (resource.exists() && resource.isReadable()){
        InputStream inputStream = resource.getInputStream();
        @SuppressWarnings("unchecked")
        Map<String, Object> yamlContent = this.yamlMapper.readValue(inputStream, Map.class);

        for (Map.Entry<String, Object> entry : yamlContent.entrySet()){
          String ruleId = entry.getKey();
          @SuppressWarnings("unchecked")
          Map<String, Object> ruleData = (Map<String, Object>) entry.getValue();

          RuleConfig ruleConfig = convertToRuleConfig(ruleData);
          sharedRules.put(ruleId, ruleConfig);
          log.debug("加载全局规则: {} from {}", ruleId, resource.getFilename());
        }
      }
    }
    return sharedRules;
  }

  // private Map<String, RuleConfig> loadShared() throws IOException {
  //   return YamlParser.load(locator.sharedResources(), RuleYamlMapper::toRuleConfig);
  // }

  private Map<String, RuleChainConfig> getRuleChains() throws IOException {
    Resource[] resources = this.resourceResolver.getResources(this.yamlRuleChainProperties.getRuleChainsPath());
    log.debug("找到规则链文件 {} 个", resources.length);

    Map<String, RuleChainConfig> ruleChainConfigMap = new HashMap<>();

    for (Resource resource : resources) {
      if (resource.exists() && resource.isReadable()) {
        try (InputStream inputStream = resource.getInputStream()) {
          @SuppressWarnings("unchecked")
          Map<String, Object> yamlContent = this.yamlMapper.readValue(inputStream, Map.class);

          for (Map.Entry<String, Object> entry : yamlContent.entrySet()) {
            String chainId = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> chainData = (Map<String, Object>) entry.getValue();

            RuleChainConfig chainConfig = convertToRuleChainConfig(chainData);
            ruleChainConfigMap.put(chainId, chainConfig);
            log.debug("加载规则链: {} from {}", chainId, resource.getFilename());
          }
        }
      }
    }
    return ruleChainConfigMap;
  }



  private  RuleConfig convertToRuleConfig(Map<String, Object> data) {
    RuleConfig.RuleConfigBuilder builder = RuleConfig.builder();

    builder.id((String) data.get("id"));
    builder.name((String) data.get("name"));
    builder.priority(getIntegerValue(data, "priority", 100));
    builder.condition((String) data.get("condition"));

    // 处理handler
    Object handleData = data.get("handlerConfig");
    if (handleData instanceof Map) {
      Map<String, Object> handleMap = (Map<String, Object>) handleData;
      RuleConfig.HandlerConfig.Type type = RuleConfig.HandlerConfig.Type.valueOf((String) handleMap.get("type"));
      String handler = (String) handleMap.get("handler");
      @SuppressWarnings("unchecked")
      Map<String, Object> parameters = (Map<String, Object>) handleMap.get("parameters");

      RuleConfig.HandlerConfig handlerConfigObj = new RuleConfig.HandlerConfig(type, handler, parameters);
      builder.handlerConfig(handlerConfigObj);
    }

    return builder.build();
  }

  @SuppressWarnings("unchecked")
  private RuleChainConfig convertToRuleChainConfig(Map<String, Object> data) {
    RuleChainConfig.RuleChainConfigBuilder builder = RuleChainConfig.builder();

    builder.id((String) data.get("id"));
    builder.name((String) data.get("name"));
    builder.version((String) data.get("version"));
    builder.description((String) data.get("description"));

    // 处理规则列表
    List<RuleConfig> rules = new ArrayList<>();
    Object rulesData = data.get("ruleConfigs");
    if (rulesData instanceof List) {
      List<Map<String, Object>> rulesList = (List<Map<String, Object>>) rulesData;
      for (Map<String, Object> ruleData : rulesList) {
        RuleConfig ruleConfig = mergeWithGlobalRule(ruleData);
        rules.add(ruleConfig);
      }
    }
    builder.ruleConfigs(rules);

    return builder.build();
  }

  private  Integer getIntegerValue(Map<String, Object> data, String key, Integer defaultValue) {
    Object value = data.get(key);
    return value != null ? (Integer) value : defaultValue;
  }

  private RuleConfig mergeWithGlobalRule(Map<String, Object> localRuleData) {
    String ruleId = (String) localRuleData.get("id");

    // 获取全局规则作为基础
    RuleConfig globalRule = sharedRules.get(ruleId);

    if (globalRule != null) {
      // 合并全局规则与局部配置
      return RuleConfig.builder()
        .id(ruleId)
        .name(getStringValue(localRuleData, "name", globalRule.name()))
        .priority(getIntegerValue(localRuleData, "priority", globalRule.priority()))
        .condition(getStringValue(localRuleData, "condition", globalRule.condition()))
        .handlerConfig(getHandlerValue(localRuleData, "handlerConfig", globalRule.handlerConfig()))
        .build();
    } else {
      // 纯局部规则
      return convertToRuleConfig(localRuleData);
    }
  }

  private  String getStringValue(Map<String, Object> data, String key, String defaultValue) {
    Object value = data.get(key);
    return value != null ? (String) value : defaultValue;
  }

  private  RuleConfig.HandlerConfig getHandlerValue(Map<String, Object> data, String key, RuleConfig.HandlerConfig defaultValue) {
    Object value = data.get(key);
    return value != null ? convertToHandler((Map<String, Object>) value) : defaultValue;
  }

  @SuppressWarnings("unchecked")
  private  RuleConfig.HandlerConfig convertToHandler(Map<String, Object> handleMap) {
    RuleConfig.HandlerConfig.Type type = RuleConfig.HandlerConfig.Type.valueOf((String) handleMap.get("type"));
    String handler = (String) handleMap.get("handler");
    Map<String, Object> parameters = (Map<String, Object>) handleMap.get("parameters");
    return new RuleConfig.HandlerConfig(type, handler, parameters);
  }

}
