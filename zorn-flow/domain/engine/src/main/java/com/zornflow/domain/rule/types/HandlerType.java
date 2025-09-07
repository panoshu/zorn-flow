package com.zornflow.domain.rule.types;

import com.domain.contract.valueobject.DomainPrimitive;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 动作类型枚举
 * 定义规则动作的处理器类型
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/25 19:50
 */

public enum HandlerType implements DomainPrimitive {
  CLASS("class"),
  SCRIPT("script"),
  JAR("jar");

  private static final Map<String, HandlerType> VALUE_MAP;

  static {
    // 为小规模枚举构建映射（O(1)查找）
    Map<String, HandlerType> map = new HashMap<>();
    for (HandlerType type : values()) {
      map.put(type.value, type);
    }
    VALUE_MAP = Collections.unmodifiableMap(map);
  }

  private final String value;

  HandlerType(String value) {
    this.value = value;
  }

  /**
   * 安全地从字符串获取动作类型（推荐使用）
   */
  public static Optional<HandlerType> tryFrom(String value) {
    if (value == null) return Optional.empty();
    return Optional.ofNullable(VALUE_MAP.get(value.toLowerCase()));
  }

  /**
   * 从字符串获取动作类型（当确定值有效时使用）
   */
  public static HandlerType from(String value) {
    return tryFrom(value)
      .orElseThrow(() -> new IllegalArgumentException("无效的动作类型: " + value));
  }

  public String value() {
    return value;
  }
}
