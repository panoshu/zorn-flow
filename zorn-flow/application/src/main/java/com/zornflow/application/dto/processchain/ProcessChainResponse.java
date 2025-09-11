package com.zornflow.application.dto.processchain;

import com.zornflow.infrastructure.config.model.ProcessNodeConfig;

import java.time.Instant;
import java.util.List;

/**
 * 用于API响应的流程链数据传输对象。
 */
public record ProcessChainResponse(
  String id,
  String name,
  String description,
  Integer version,
  List<ProcessNodeConfig> nodes,
  Instant createdAt,
  Instant updatedAt
) {
}
