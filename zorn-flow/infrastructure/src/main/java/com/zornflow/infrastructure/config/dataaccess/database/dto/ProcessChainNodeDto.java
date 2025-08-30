package com.zornflow.infrastructure.config.dataaccess.database.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 流程链-节点关联数据库实体DTO
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProcessChainNodeDto (
  Long id,
  String processChainId,
  String nodeId,
  String nodeName,
  String nextNodeId,
  String nodeType,
  String ruleChainId,
  Map<String, Object> properties,
  Boolean isGlobalReference,
  Integer nodeOrder,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {}
