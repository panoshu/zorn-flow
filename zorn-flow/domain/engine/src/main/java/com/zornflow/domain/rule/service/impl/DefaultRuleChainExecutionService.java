package com.zornflow.domain.rule.service.impl;

import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.rule.entity.Rule;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.service.*;

import java.util.*;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/1 12:22
 */

public class DefaultRuleChainExecutionService implements RuleChainExecutionService {

  private final ConditionEvaluator conditionEvaluator;
  private final HandlerExecutorFactory handlerExecutorFactory;

  public DefaultRuleChainExecutionService(ConditionEvaluator conditionEvaluator, HandlerExecutorFactory handlerExecutorFactory) {
    if (conditionEvaluator == null || handlerExecutorFactory == null) {
      throw new IllegalArgumentException("ConditionEvaluator and HandlerExecutorFactory must not be null.");
    }
    this.conditionEvaluator = conditionEvaluator;
    this.handlerExecutorFactory = handlerExecutorFactory;
  }

  /**
   * 执行一个规则链
   * @param ruleChain 规则链定义
   * @param context   当前的业务上下文
   * @return 执行完毕后，可能被修改过的业务上下文
   */
  @Override
  public BusinessContext execute(RuleChain ruleChain, BusinessContext context) {
    if (ruleChain == null || context == null) {
      // 或者根据业务需求返回 context
      throw new IllegalArgumentException("RuleChainDefinition and BusinessContext must not be null.");
    }

    // 1. 按优先级对规则进行排序（数字越小，优先级越高）
    var sortedRules = ruleChain.getRules().stream()
      .sorted(Comparator.comparing(rule -> rule.getPriority().value()))
      .toList();

    for (Rule rule : sortedRules) {
      // 2. 使用ConditionEvaluator SPI进行条件判断
      if (conditionEvaluator.evaluate(rule.getCondition(), context)) {

        // 3. 使用HandlerExecutorFactory SPI获取对应的执行器
        var executor = handlerExecutorFactory.getExecutor(rule.getHandlerConfig())
          .orElseThrow(() -> new IllegalStateException("No handler executor found for type: " + rule.getHandlerConfig().type()));

        // 4. 执行Handler，Handler可能会修改上下文
        // 注意：由于BusinessContext是不可变的record，execute方法需要返回一个新的实例
        executor.execute(rule.getHandlerConfig(), context);
      }
    }

    // 5. 返回最终的上下文
    return context;
  }
}
