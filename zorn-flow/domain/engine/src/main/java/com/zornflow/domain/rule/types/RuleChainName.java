package com.zornflow.domain.rule.types;

import com.ddd.contract.valueobject.DomainPrimitive;

/**
 * 规则链名称领域原语
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 13:29
 */

public record RuleChainName(String value) implements DomainPrimitive {
  public RuleChainName {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("规则链名称不能为空");
    }
    if (value.length() > 40) {
      throw new IllegalArgumentException("规则链名称长度不能超过40个字符");
    }
  }

  /**
   * 从字符串创建规则链名称
   * @param value 字符串值
   * @return 规则链名称实例
   */
  public static RuleChainName of(String value) {
    return new RuleChainName(value);
  }

  public static RuleChainName of(RuleChainId ruleChainId) {
    return new RuleChainName(ruleChainId.value());
  }
}
