package com.zornflow.application.dto.rulechain;

import com.zornflow.infrastructure.config.model.RuleConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateRuleChainRequest(
  @NotBlank @Size(max = 40) String id,
  @NotBlank @Size(max = 40) String name,
  String description,
  @NotEmpty List<RuleConfig> rules
) {
}
