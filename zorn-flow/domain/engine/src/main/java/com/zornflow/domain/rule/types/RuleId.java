package com.zornflow.domain.rule.types;

import com.domain.contract.valueobject.DomainPrimitive;
import com.domain.contract.valueobject.Identifier;

/**
 * 规则ID 领域原语
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/25 12:36
 */
public record RuleId(String value) implements DomainPrimitive, Identifier {

  public RuleId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("规则ID不能为空");
    }
    if (value.length() > 40) {
      throw new IllegalArgumentException("规则ID长度不能超过40个字符");
    }
  }

  public static RuleId of(String value) {
    return new RuleId(value);
  }

}
