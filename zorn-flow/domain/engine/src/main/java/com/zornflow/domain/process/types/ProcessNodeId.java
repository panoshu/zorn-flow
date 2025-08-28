package com.zornflow.domain.process.types;

import com.domain.contract.valueobject.Identifier;
import com.domain.contract.valueobject.DomainPrimitive;

/**
 * 流程节点 ID 领域原语
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 16:13
 */
public record ProcessNodeId(String value) implements DomainPrimitive, Identifier {
  public ProcessNodeId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("流程ID不能为空");
    }
    if (value.length() > 40) {
      throw new IllegalArgumentException("流程ID长度不能超过40个字符");
    }
  }

  /**
   * 从字符串创建流程ID
   * @param value 字符串值
   * @return 流程ID实例
   */
  public static ProcessNodeId of(String value) {
    return new ProcessNodeId(value);
  }

}
