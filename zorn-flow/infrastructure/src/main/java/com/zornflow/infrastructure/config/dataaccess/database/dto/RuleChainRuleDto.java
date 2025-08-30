package com.zornflow.infrastructure.config.dataaccess.database.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 规则链-规则关联数据库实体DTO
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RuleChainRuleDto(
  Long id,
  String ruleChainId,
  String ruleId,
  String ruleName,
  Integer priority,
  String conditionExpr,
  String handlerType,
  String handlerClass,
  Map<String, Object> handlerParameters,
  Boolean isGlobalReference,
  Integer ruleOrder,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
}
