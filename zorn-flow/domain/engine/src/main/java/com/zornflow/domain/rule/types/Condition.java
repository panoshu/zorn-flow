package com.zornflow.domain.rule.types;

import com.ddd.contract.valueobject.DomainPrimitive;

/**
 * 条件表达式领域原语
 * 封装规则的EL表达式条件
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/25 13:53
 */

public record Condition(String expression) implements DomainPrimitive {
  public Condition {
    if (expression == null || expression.isBlank()) {
      expression = "#{true}";
    }
    if (!expression.startsWith("#{")) {
      throw new IllegalArgumentException("条件表达式必须以'#{开头'");
    }
    if (!expression.endsWith("}")) {
      throw new IllegalArgumentException("条件表达式必须以'}结尾'");
    }
  }

  /**
   * 从表达式字符串创建条件对象
   * @param expression EL表达式
   * @return 条件对象
   */
  public static Condition of(String expression) {
    if (expression == null || expression.isBlank()) {
      return new Condition("#{true}");
    }
    return new Condition(expression);
  }

  /**
   * 获取纯表达式内容（去除前后的#{和}）
   * @return 纯表达式字符串
   */
  public String getPureExpression() {
    return expression.substring(2, expression.length() - 1);
  }
}
