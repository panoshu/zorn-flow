package matcher;

import java.io.Serializable;
import java.util.List;

/**
 * 匹配器 (Value Object)
 * 一个不可变对象，用于封装匹配逻辑。
 * @param conditions 字段比较条件列表
 * @param expression SpEL 表达式字符串
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/18 12:32
 */

public record Matcher(List<FieldCondition> conditions, String expression) implements Serializable {

  /**
   * 字段比较条件 (Value Object)
   * @param field 字段名，支持点状导航，如 "customer.level"
   * @param operator 比较操作符
   * @param value 期望值
   */
  public record FieldCondition(String field, Operator operator, String value) implements Serializable {}

  /**
   * 比较操作符枚举
   */
  public enum Operator {
    EQ, // 等于
    NE, // 不等于
    GT, // 大于
    LT, // 小于
    GTE, // 大于等于
    LTE, // 小于等于
    IN // 包含
  }
}
