package com.zornflow.domain.rule.service;

import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.rule.types.Condition;

/**
 * 条件表达式求值器接口
 * 负责解析和执行规则中的Condition表达式
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 22:58
 **/

public interface ConditionEvaluator {
  boolean evaluate(Condition condition, BusinessContext context);
}
