package com.zornflow.domain.rule.entity;

import com.domain.contract.aggregate.Entity;
import com.zornflow.domain.rule.types.Condition;
import com.zornflow.domain.rule.types.Priority;
import com.zornflow.domain.rule.types.RuleId;
import com.zornflow.domain.rule.types.RuleName;
import com.zornflow.domain.rule.valueobject.HandlerConfig;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

/**
 * 规则实体
 * 领域模型中的核心实体，封装规则的完整信息和行为
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/25 20:31
 */

@Getter
public class Rule extends Entity<RuleId> {
  private final RuleName name;
  private final Priority priority;
  private final Condition condition;
  private final HandlerConfig handlerConfig;

  @Builder
  private Rule(RuleId id, RuleName name, Priority priority, Condition condition, HandlerConfig handlerConfig) {
    super(Objects.requireNonNull(id, "Rule ID must not be null"));
    this.name = name != null ? name : RuleName.of(id);
    this.priority = Objects.requireNonNull(priority, "Priority must not be null");
    this.condition = Objects.requireNonNull(condition, "Condition must not be null");
    this.handlerConfig = Objects.requireNonNull(handlerConfig, "Handler config must not be null");
    validateInvariants();
  }

  @Override
  protected void validateInvariants() {

  }
}
