package com.zornflow.domain.process.valueobject;

import com.domain.contract.valueobject.BaseValueObject;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.domain.rule.types.Condition;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 22:21
 **/

public record GatewayCondition(
  Condition condition,
  ProcessNodeId nextNodeId
) implements BaseValueObject {
  public static GatewayCondition of(Condition condition, ProcessNodeId nextNodeId) {
    return new GatewayCondition(condition, nextNodeId);
  }
}
