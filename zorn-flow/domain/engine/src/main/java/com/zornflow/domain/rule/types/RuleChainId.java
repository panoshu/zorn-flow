package com.zornflow.domain.rule.types;

import com.domain.contract.valueobject.EntityId;

/**
 * 规则链 ID 领域原语
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 13:24
 */

public record RuleChainId(String value) implements EntityId {
  public RuleChainId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("规则链ID不能为空");
    }
    if (value.length() > 40) {
      throw new IllegalArgumentException("规则链ID长度不能超过40个字符");
    }
  }

  public static RuleChainId of(String raw) {
    return new RuleChainId(raw);
  }

}
