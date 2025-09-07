package com.zornflow.infrastructure.adapter.gateway;

import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.process.entity.ProcessNode;
import com.zornflow.domain.process.service.GatewayProcessor;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.domain.process.valueobject.GatewayCondition;
import com.zornflow.domain.process.valueobject.NodeType;
import com.zornflow.domain.rule.service.ConditionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * GatewayProcessor 的默认实现。
 * 它按顺序评估网关节点中定义的所有条件，并返回第一个满足条件的分支。
 * 如果没有任何条件满足，它将返回节点本身定义的 nextNodeId 作为默认路径。
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultGatewayProcessor implements GatewayProcessor {

  private final ConditionEvaluator conditionEvaluator;

  @Override
  public ProcessNodeId process(ProcessNode gatewayNode, BusinessContext context) {
    log.debug("开始处理网关节点: {}", gatewayNode.getId().value());
    validateGatewayNode(gatewayNode);

    // 1. 查找第一个匹配的条件分支
    ProcessNodeId matched = findMatchedCondition(gatewayNode, context);

    if (matched != null) {
      log.info("网关 [{}] 条件匹配成功，流程将转向节点 [{}]", gatewayNode.getId().value(), matched.value());
      return matched;
    }

    // 2. 如果没有匹配，则走默认分支
    return handleDefault(gatewayNode);
  }

  private void validateGatewayNode(ProcessNode node) {
    if (node.getType() != NodeType.GATEWAY) {
      throw new IllegalArgumentException(
        String.format("节点 [%s] 类型错误，预期为 GATEWAY，实际为 %s",
          node.getId().value(), node.getType())
      );
    }
  }

  private ProcessNodeId findMatchedCondition(ProcessNode gatewayNode, BusinessContext context) {
    for (GatewayCondition condition : gatewayNode.getConditions()) {
      log.trace("正在评估条件: '{}' -> 目标节点 '{}'",
        condition.condition().expression(), condition.nextNodeId().value());

      if (conditionEvaluator.evaluate(condition.condition(), context)) {
        return condition.nextNodeId();
      }
    }
    return null;
  }

  private ProcessNodeId handleDefault(ProcessNode gatewayNode) {
    ProcessNodeId defaultNextNode = gatewayNode.getNextNodeId();
    if (defaultNextNode != null) {
      log.info("网关 [{}] 所有条件均不匹配，流程将转向默认节点 [{}]",
        gatewayNode.getId().value(), defaultNextNode.value());
    } else {
      log.warn("网关 [{}] 所有条件均不匹配，且未配置默认的下一个节点。流程将在此终止。",
        gatewayNode.getId().value());
    }
    return defaultNextNode;
  }
}
