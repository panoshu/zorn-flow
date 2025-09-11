package com.zornflow.application.dto.rulechain;

import com.zornflow.infrastructure.config.model.RuleConfig;

import java.time.Instant;
import java.util.List;

// 注意：为了简化，我们直接复用 RuleConfig DTO。在复杂项目中，这里也应该有独立的 Response DTO。
public record RuleChainResponse(
  String id,
  String name,
  String description,
  Integer version,
  List<RuleConfig> rules,
  Instant createdAt,
  Instant updatedAt
) {
}
