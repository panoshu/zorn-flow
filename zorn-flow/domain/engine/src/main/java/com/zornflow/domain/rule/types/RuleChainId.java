package com.zornflow.domain.rule.types;

import com.ddd.contract.valueobject.Identifier;
import com.zornflow.domain.common.types.BaseIdentifier;
import com.ddd.contract.valueobject.DomainPrimitive;

/**
 * 规则链 ID 领域原语
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 13:24
 */

public record RuleChainId(String  value) implements DomainPrimitive, Identifier {
  public RuleChainId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("规则链ID不能为空");
    }
    if (value.length() > 40) {
      throw new IllegalArgumentException("规则链ID长度不能超过40个字符");
    }
  }

  /**
   * 从字符串创建规则链ID
   * @param value 字符串值
   * @return 规则链ID实例
   */
  public static RuleChainId of(String value) {
    return new RuleChainId(value);
  }

}
