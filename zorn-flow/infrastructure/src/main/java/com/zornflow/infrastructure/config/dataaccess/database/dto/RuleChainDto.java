package com.zornflow.infrastructure.config.dataaccess.database.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * 规则链数据库实体DTO
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RuleChainDto(
  String id,
  String name,
  String version,
  String description,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
}
