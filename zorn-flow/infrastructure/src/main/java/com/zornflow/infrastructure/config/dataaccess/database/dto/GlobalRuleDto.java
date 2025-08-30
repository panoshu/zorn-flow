package com.zornflow.infrastructure.config.dataaccess.database.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 全局规则数据库实体DTO
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GlobalRuleDto(
  String id,
  String name,
  Integer priority,
  String conditionExpr,
  String handlerType,
  String handlerClass,
  Map<String, Object> handlerParameters,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
}
