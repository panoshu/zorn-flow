package com.zornflow.domain.rule.entity;

import com.domain.contract.aggregate.Entity;
import com.zornflow.domain.rule.types.Condition;
import com.zornflow.domain.rule.types.Priority;
import com.zornflow.domain.rule.types.RuleId;
import com.zornflow.domain.rule.types.RuleName;
import com.zornflow.domain.rule.valueobject.Handler;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

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
  private final Handler handler;

  @Builder
  private Rule(RuleId id, RuleName name, Priority priority, Condition condition, Handler handler) {
    super(Objects.requireNonNull(id, "Rule ID must not be null"));
    this.name = Optional.ofNullable(name).orElse(RuleName.of(id));

    this.handler = Objects.requireNonNull(handler, "Handler config must not be null");
    this.priority = Optional.ofNullable(priority).orElse(Priority.defaultPriority());

    this.condition = condition;

    validateInvariants();
  }

  @Override
  protected void validateInvariants() {

  }

  @Override
  public Instant getCreatedAt() {
    return super.getCreatedAt();
  }

  @Override
  public Instant getUpdatedAt() {
    return super.getUpdatedAt();
  }
}
