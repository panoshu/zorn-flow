package com.zornflow.infrastructure.config.model;

import com.zornflow.domain.common.config.model.ModelConfig;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
  Map<String, Object> properties,
  Optional<String> sharedNodeId
) implements ModelConfig {

  public enum NodeType {
    BUSINESS, APPROVAL, GATEWAY
  }

  public record GatewayConditionConfig(
    String condition,
    String next) {
  }

  public ProcessNodeConfig mergeWithDefaults(ProcessNodeConfig defaults) {
    if (defaults == null) return this;
    return ProcessNodeConfig.builder()
      .id(this.id)
      .name(Optional.ofNullable(this.name).orElse(defaults.name()))
      .next(this.next)
      .type(Optional.ofNullable(this.type).orElse(defaults.type()))
      .ruleChain(Optional.ofNullable(this.ruleChain).orElse(defaults.ruleChain()))
      .conditions(Optional.ofNullable(this.conditions).orElse(defaults.conditions()))
      .properties(Optional.ofNullable(this.properties).orElse(defaults.properties()))
      .sharedNodeId(Optional.of(defaults.id()))
      .build();
  }
}
