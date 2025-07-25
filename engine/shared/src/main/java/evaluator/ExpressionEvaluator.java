package evaluator;

import context.ContextView;

/**
 * 表达式求值器接口 (防腐层抽象)
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/18 12:40
 */
public interface ExpressionEvaluator {
  boolean evaluate(String expression, ContextView context);
}
