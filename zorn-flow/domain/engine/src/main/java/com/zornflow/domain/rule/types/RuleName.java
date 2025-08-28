package com.zornflow.domain.rule.types;

import com.domain.contract.valueobject.DomainPrimitive;

/**
 * 规则名称领域原语
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/25 13:52
 */

public record RuleName(String value) implements DomainPrimitive {
  public RuleName {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("规则名称不能为空");
    }
    if (value.length() > 40) {
      throw new IllegalArgumentException("规则名称不能超过40个字符");
    }
  }

  /**
   * 从字符串创建规则名称
   * @param value 字符串值
   * @return 规则名称
   */
  public static RuleName of(String value) {
    return new RuleName(value);
  }

  public static RuleName of(RuleId ruleId) {
    return new RuleName(ruleId.value());
  }
}
