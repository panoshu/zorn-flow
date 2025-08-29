package com.zornflow.infrastructure.config.model;

import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/28 13:21
 */
@Builder
public record ProcessNodeConfig(
  String id,
  String name,
  String next,
  NodeType type,
  String ruleChain,
  List<GatewayConditionConfig> conditions,
  Map<String, Object> properties) implements EngineModelConfigDTO {

  public enum NodeType {BUSINESS, APPROVAL, GATEWAY}
}
