package com.zornflow.domain.rule.service;

import com.zornflow.domain.common.context.FlowContext;
import com.zornflow.domain.rule.entity.Rule;
import com.zornflow.domain.rule.valueobject.RuleExecutionResult;

/**
 * 规则执行领域服务接口
 * 定义规则执行的核心操作
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 18:10
 */

public interface RuleExecutionService {
  /**
   * 执行单个规则
   * @param rule 要执行的规则
   * @param context 业务上下文
   * @return 规则执行结果
   */
  RuleExecutionResult executeRule(Rule rule, FlowContext context);

  /**
   * 评估规则条件
   * @param rule 要评估的规则
   * @param context 业务上下文
   * @return true表示条件满足
   */
  boolean evaluateCondition(Rule rule, FlowContext context);

  /**
   * 执行规则动作
   * @param rule 要执行的规则
   * @param context 业务上下文
   * @return 执行后的业务上下文
   */
  FlowContext executeAction(Rule rule, FlowContext context);
}
