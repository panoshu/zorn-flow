package evaluator;

import context.ContextView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/18 13:24
 */

@Slf4j
public class SpelExpressionEvaluator implements ExpressionEvaluator {

  private final ExpressionParser expressionParser = new SpelExpressionParser();

  @Override
  public boolean evaluate(String expression, ContextView context) {
    try {
      var evaluationContext = new StandardEvaluationContext(context);
      Boolean result = expressionParser.parseExpression(expression).getValue(evaluationContext, Boolean.class);
      return Boolean.TRUE.equals(result);
    } catch (Exception e) {
      // log.error(...)
      return false;
    }
  }
}
