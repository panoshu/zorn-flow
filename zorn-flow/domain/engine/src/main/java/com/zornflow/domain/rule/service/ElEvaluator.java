package com.zornflow.domain.rule.service;

import com.zornflow.domain.rule.config.RuleConfig;

import java.util.Map;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/1 12:12
 */

public interface ElEvaluator {

  /**
   * 评估EL表达式
   * @param elExpression EL表达式字符串
   * @param context 评估上下文
   * @return 表达式计算结果
   */
  boolean evaluate(String elExpression, Map<String, Object> context);

  /**
   * 判断规则是否应该执行
   * @param rule 规则配置
   * @param context 业务上下文
   * @return 如果条件满足则返回true，否则返回false
   */
  boolean shouldExecuteRule(RuleConfig rule, Map<String, Object> context);
}
