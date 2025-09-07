package com.zornflow.infrastructure.adapter.evaluator;

import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.rule.service.ConditionEvaluator;
import com.zornflow.domain.rule.types.Condition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/18 13:24
 */

@Slf4j
@Service
public class SpelConditionEvaluator implements ConditionEvaluator {

  private static final SpelExpressionParser PARSER = new SpelExpressionParser();

  @Override
  public boolean evaluate(Condition condition, BusinessContext context) {
    if (condition == null || condition.expression() == null || condition.expression().isBlank()) {
      return false; // 或者根据业务定义返回true
    }
    try {
      var expression = PARSER.parseExpression(condition.expression());
      // 将BusinessContext中的数据作为SpEL的变量
      var evaluationContext = new StandardEvaluationContext();
      evaluationContext.setVariables(context.data());

      Boolean result = expression.getValue(evaluationContext, Boolean.class);
      return Boolean.TRUE.equals(result);
    } catch (Exception e) {
      // 实际项目中应记录详细日志
      log.error("Error evaluating SpEL expression: {} | Error: {}", condition.expression(), e.getMessage());
      return false;
    }
  }
}
