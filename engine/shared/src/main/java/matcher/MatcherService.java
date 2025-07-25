package matcher;

import context.ContextView;
import evaluator.ExpressionEvaluator;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * 匹配领域服务 (Domain Service)
 * 负责执行匹配逻辑，判断一个 Matchable 对象是否与给定的业务上下文匹配。
 * 无状态，并发安全
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/18 12:38
 */

@RequiredArgsConstructor
public class MatcherService {

  private final ExpressionEvaluator expressionEvaluator;

  /**
   * 核心匹配方法
   * @param target 需要进行匹配判断的目标对象
   * @param contextView 提供业务数据的上下文视图
   * @return true 如果匹配成功, false 如果不匹配或 target/matcher 为 null
   */
  public boolean isMatch(Matchable target, ContextView contextView) {
    if (target == null || target.getMatcher() == null) {
      // 如果目标或其匹配器为null，可以根据业务定义为 true 或 false。通常为 true，表示“通用，无需匹配”。
      return true;
    }

    Matcher matcher = target.getMatcher();

    // 1. 检查 SpEL 表达式
    if (matcher.expression() != null && !matcher.expression().isBlank()) {
      expressionEvaluator.evaluate(matcher.expression(), contextView);
    }

    // 2. 如果没有表达式，则检查字段条件（此处简化为需要全部满足 AND 逻辑）
    // 在实际实现中，可以增加一个字段来控制是AND还是OR
    if (matcher.conditions() != null && !matcher.conditions().isEmpty()) {
      return matcher.conditions().stream().allMatch(
        condition -> checkFieldCondition(condition, contextView)
      );
    }

    // 3. 如果既没有表达式也没有条件，则认为是通用匹配
    return true;
  }

  private boolean checkFieldCondition(Matcher.FieldCondition condition, ContextView contextView) {
    Object actualValue = contextView.getProperty(condition.field());
    if (actualValue == null) {
      return false;
    }

    final boolean equals = Objects.equals(String.valueOf(actualValue), condition.value());
    return switch (condition.operator()) {
      case EQ ->  equals;
      case NE ->  !equals;
      case GT ->  compare(String.valueOf(actualValue), condition.value()) > 0;
      case LT ->  compare(String.valueOf(actualValue), condition.value()) < 0;
      case GTE -> compare(String.valueOf(actualValue), condition.value()) >= 0;
      case LTE -> compare(String.valueOf(actualValue), condition.value()) <= 0;
      case IN ->  contains(String.valueOf(actualValue), condition.value());
    };
  }

  private int compare(Object a, Object b) {
    if (a instanceof Comparable<?> && b instanceof Comparable<?>) {
      if (a.getClass().isAssignableFrom(b.getClass()) || b.getClass().isAssignableFrom(a.getClass())) {
        @SuppressWarnings("unchecked")
        Comparable<Object> comparableA = (Comparable<Object>) a;
        return comparableA.compareTo(b);
      }
    }
    return String.valueOf(a).compareTo(String.valueOf(b));
  }

  private boolean contains(Object collection, Object value) {
    if (collection instanceof Collection<?>) {
      return ((Collection<?>) collection).contains(value);
    } else if (collection instanceof String) {
      return Arrays.asList(((String) collection).split(",")).contains(String.valueOf(value));
    }
    return false;
  }
}
