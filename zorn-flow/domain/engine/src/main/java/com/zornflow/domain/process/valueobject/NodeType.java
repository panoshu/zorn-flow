package com.zornflow.domain.process.valueobject;

import com.domain.contract.valueobject.BaseValueObject;
import lombok.Getter;

/**
 * 流程节点类型枚举
 * 定义流程中节点的各种类型
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 16:19
 */

@Getter
public enum NodeType implements BaseValueObject {
  BUSINESS("business"),
  APPROVAL("approval"),
  GATEWAY("gateway");

  private final String value;

  NodeType(String value) {
    this.value = value;
  }

  /**
   * 根据字符串值获取对应的枚举
   * @param value 字符串值
   * @return 对应的枚举实例
   * @throws IllegalArgumentException 如果值无效
   */
  public static NodeType fromValue(String value) {
    for (NodeType type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("无效的节点类型: " + value);
  }
}
