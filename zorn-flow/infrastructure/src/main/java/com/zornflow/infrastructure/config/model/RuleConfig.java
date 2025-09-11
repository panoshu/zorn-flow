package com.zornflow.infrastructure.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zornflow.domain.common.config.model.ModelConfig;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/28 13:20
 */
@Builder
public record RuleConfig(
  String id,
  String name,
  @JsonProperty(defaultValue = "100") Integer priority,
  String condition,
  HandlerConfig handle,
  Optional<String> sharedRuleId,
  String status,
  Integer version,
  OffsetDateTime createdAt,
  OffsetDateTime updatedAt
) implements ModelConfig {

  public RuleConfig mergeWithDefaults(RuleConfig defaults) {
    if (defaults == null) return this;
    return RuleConfig.builder()
      .id(defaults.id())
      .name(Optional.ofNullable(this.name).orElse(defaults.name()))
      .priority(Optional.ofNullable(this.priority).orElse(defaults.priority()))
      .condition(Optional.ofNullable(this.condition).orElse(defaults.condition()))
      .handle(Optional.ofNullable(this.handle).orElse(defaults.handle()))
      .sharedRuleId(Optional.of(defaults.id()))
      .status(RecordStatus.ACTIVE.getDbValue())
      .version(this.version)
      .createdAt(this.createdAt)
      .updatedAt(this.updatedAt)
      .build();
  }

  public record HandlerConfig(Type type, String handler, Map<String, Object> parameters) {
    public enum Type {CLASS, SCRIPT, JAR}
  }
}
