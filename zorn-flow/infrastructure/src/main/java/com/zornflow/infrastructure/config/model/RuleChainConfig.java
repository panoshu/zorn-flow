package com.zornflow.infrastructure.config.model;

import lombok.Builder;

import java.util.List;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/28 13:21
 */
@Builder
public record RuleChainConfig(
  String id,
  String name,
  String version,
  String description,
  List<RuleConfig> rules) implements EngineModelConfigDTO { }
