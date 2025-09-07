package com.zornflow.domain.process.service.impl;

import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.entity.ProcessInstance;
import com.zornflow.domain.process.entity.ProcessNode;
import com.zornflow.domain.process.repository.ProcessChainRepository;
import com.zornflow.domain.process.service.GatewayProcessor;
import com.zornflow.domain.process.service.ProcessOrchestrationService;
import com.zornflow.domain.process.valueobject.NodeType;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.repository.RuleChainRepository;
import com.zornflow.domain.rule.service.RuleChainExecutionService;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 23:13
 **/
@RequiredArgsConstructor
public class DefaultProcessOrchestrationService implements ProcessOrchestrationService {
  private final ProcessChainRepository definitionRepository;
  private final RuleChainRepository ruleChainRepository;
  private final RuleChainExecutionService ruleChainExecutionService;
  private final GatewayProcessor gatewayProcessor;

  /**
   * 执行流程实例下一步
   *
   * @param instance 流程实例聚合根（将被直接修改）
   */
  public void executeNextStep(ProcessInstance instance) {
    Objects.requireNonNull(instance, "ProcessInstance cannot be null.");

    // 1. 获取流程定义
    ProcessChain definition = definitionRepository.findById(instance.getProcessChainId())
      .orElseThrow(() -> new IllegalStateException("Cannot find process definition: " + instance.getProcessChainId()));

    // 2. 获取当前节点定义
    ProcessNode currentNode = definition.getNodeById(instance.getCurrentNodeId());

    // 3. 使用 JDK 21 的模式匹配 switch 处理不同类型的节点
    switch (currentNode.getType()) {
      case NodeType.BUSINESS, NodeType.APPROVAL -> handleBusinessNode(instance, currentNode);
      case NodeType.GATEWAY -> handleGatewayNode(instance, currentNode);
      default -> throw new UnsupportedOperationException("Unsupported node type: " + currentNode.getType());
    }
  }

  /**
   * 处理业务节点（包括审批节点，因其核心逻辑也是执行规则链）
   */
  private void handleBusinessNode(ProcessInstance instance, ProcessNode node) {
    BusinessContext finalContext = instance.getContext();

    if (node.getRuleChainId() != null) {
      RuleChain ruleChain = ruleChainRepository.findById(node.getRuleChainId())
        .orElseThrow(() -> new IllegalStateException("RuleChainDefinition not found: " + node.getRuleChainId()));
      finalContext = ruleChainExecutionService.execute(ruleChain, instance.getContext());
    }

    instance.moveToNextNode(node.getNextNodeId(), finalContext);
  }

  /**
   * 处理网关节点
   */
  private void handleGatewayNode(ProcessInstance instance, ProcessNode node) {
    // 委托给 GatewayProcessor SPI 来决定下一个节点
    var nextNodeId = gatewayProcessor.process(node, instance.getContext());

    // 更新实例状态，移动到网关决策出的下一个节点
    instance.moveToNextNode(nextNodeId, instance.getContext());
  }

}
