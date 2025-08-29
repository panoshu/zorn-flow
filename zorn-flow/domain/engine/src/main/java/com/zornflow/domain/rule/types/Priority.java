package com.zornflow.domain.rule.types;

import com.domain.contract.valueobject.DomainPrimitive;

/**
 * 优先级领域原语
 * 数值越小优先级越高，默认值为100
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/25 13:41
 */
public record Priority(int value) implements DomainPrimitive, Comparable<Priority> {
  public Priority {
    if (value < 0) {
      throw new IllegalArgumentException("优先级必须为正整数");
    }
  }

  /**
   * 创建优先级实例
   *
   * @param value 优先级数值
   * @return 优先级对象
   */
  public static Priority of(int value) {
    return new Priority(value);
  }

  /**
   * 获取默认优先级(100)
   *
   * @return 默认优先级对象
   */
  public static Priority defaultPriority() {
    return new Priority(50);
  }

  /**
   * 比较优先级：值越大优先级越高
   *
   * @return 负数表示 this 优先级更高（应排在前面）
   */
  @Override
  public int compareTo(Priority other) {
    return Integer.compare(other.value, this.value);
  }

  public boolean isHigherThan(Priority other) {
    return this.compareTo(other) < 0;
  }

  public boolean isLowerThan(Priority other) {
    return this.compareTo(other) > 0;
  }
}
