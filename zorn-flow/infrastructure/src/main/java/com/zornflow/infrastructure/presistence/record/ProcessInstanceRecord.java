package com.zornflow.infrastructure.presistence.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/30 14:28
 **/

@Builder
public record ProcessInstanceRecord(
  String id,
  String processChainId,
  String status,
  String currentNodeId,
  String context,
  LocalDateTime createdAt,
  LocalDateTime updatedAt,
  Long version
) {}
