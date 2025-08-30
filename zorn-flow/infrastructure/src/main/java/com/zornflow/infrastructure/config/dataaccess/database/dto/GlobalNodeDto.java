package com.zornflow.infrastructure.config.dataaccess.database.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 全局节点数据库实体DTO
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GlobalNodeDto (
  String id,
  String name,
  String nextNodeId,
  String nodeType,
  String ruleChainId,
  Map<String, Object> properties,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {}
