package com.zornflow.infrastructure.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Map;

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
  Handler handle
) implements EngineModelConfigDTO {

  public record Handler(Type type, String handler, Map<String, Object> parameters) {
    public enum Type {CLASS, SCRIPT, JAR}
  }
}
