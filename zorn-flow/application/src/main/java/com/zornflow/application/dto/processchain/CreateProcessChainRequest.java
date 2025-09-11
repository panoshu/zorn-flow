package com.zornflow.application.dto.processchain;

import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 创建新流程链的API请求体。
 */
public record CreateProcessChainRequest(
  @NotBlank @Size(max = 40) String id,
  @NotBlank @Size(max = 40) String name,
  String description,
  @NotEmpty List<ProcessNodeConfig> nodes
) {
}
