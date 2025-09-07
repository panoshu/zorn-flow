package com.zornflow.domain.process.service.impl;

import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.entity.ProcessInstance;
import com.zornflow.domain.process.entity.ProcessNode;
import com.zornflow.domain.process.repository.ProcessChainRepository;
import com.zornflow.domain.process.service.GatewayProcessor;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.domain.process.valueobject.NodeType;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.repository.RuleChainRepository;
import com.zornflow.domain.rule.service.RuleChainExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultProcessOrchestrationService 领域服务单元测试")
class DefaultProcessOrchestrationServiceTest {

  @Mock private ProcessChainRepository processChainRepository;
  @Mock private RuleChainRepository ruleChainRepository;
  @Mock private RuleChainExecutionService ruleChainExecutionService;
  @Mock private GatewayProcessor gatewayProcessor;
  @Mock private ProcessInstance instance;

  @InjectMocks
  private DefaultProcessOrchestrationService orchestrationService;

  private final ProcessChainId chainId = ProcessChainId.of("p-chain-1");
  private final com.zornflow.domain.rule.types.RuleChainId ruleChainId = com.zornflow.domain.rule.types.RuleChainId.of("r-chain-1");
  private final ProcessNodeId currentNodeId = ProcessNodeId.of("node-current");
  private final ProcessNodeId nextNodeId = ProcessNodeId.of("node-next");
  private BusinessContext originalContext;
  private BusinessContext updatedContext;

  @BeforeEach
  void setUp() {
    originalContext = new BusinessContext(Map.of("status", "started"));
    updatedContext = new BusinessContext(Map.of("status", "processed"));
    when(instance.getProcessChainId()).thenReturn(chainId);
  }

  @Test
  @DisplayName("executeNextStep: 处理带规则链的业务节点，应执行规则并流转到下一节点")
  void shouldExecuteRulesAndMove_forBusinessNodeWithRuleChain() {
    // Arrange
    ProcessNode businessNode = createTestNode(NodeType.BUSINESS, nextNodeId, ruleChainId);
    ProcessChain processChain = createTestProcessChain(businessNode);
    RuleChain ruleChain = mock(RuleChain.class);

    when(instance.getCurrentNodeId()).thenReturn(currentNodeId);
    when(instance.getContext()).thenReturn(originalContext);
    when(processChainRepository.findById(chainId)).thenReturn(Optional.of(processChain));
    when(ruleChainRepository.findById(ruleChainId)).thenReturn(Optional.of(ruleChain));
    when(ruleChainExecutionService.execute(ruleChain, originalContext)).thenReturn(updatedContext);

    // Act
    orchestrationService.executeNextStep(instance);

    // Assert
    verify(ruleChainExecutionService).execute(ruleChain, originalContext);
    verify(instance).moveToNextNode(nextNodeId, updatedContext);
  }

  @Test
  @DisplayName("executeNextStep: 处理不带规则链的业务节点，应直接流转到下一节点")
  void shouldMoveDirectly_forBusinessNodeWithoutRuleChain() {
    // Arrange
    // 1. 创建一个 Mock 节点，这是正确的，因为我们想精确控制它的行为
    ProcessNode businessNode = mock(ProcessNode.class);

    // 2. [关键修正] 为 Mock 对象的必要方法提供返回值
    //    告诉 Mockito: 当调用 getId() 时，返回 currentNodeId
    when(businessNode.getId()).thenReturn(currentNodeId);
    when(businessNode.getRuleChainId()).thenReturn(null);
    when(businessNode.getNextNodeId()).thenReturn(nextNodeId);
    when(businessNode.getType()).thenReturn(NodeType.BUSINESS);

    // 3. 使用这个配置好的 Mock 节点创建 ProcessChain
    ProcessChain processChain = createTestProcessChain(businessNode);

    when(instance.getCurrentNodeId()).thenReturn(currentNodeId);
    when(instance.getContext()).thenReturn(originalContext);
    when(processChainRepository.findById(chainId)).thenReturn(Optional.of(processChain));

    // Act
    orchestrationService.executeNextStep(instance);

    // Assert
    verify(ruleChainExecutionService, never()).execute(any(), any()); // 验证从未执行规则
    verify(instance).moveToNextNode(nextNodeId, originalContext);
  }

  @Test
  @DisplayName("executeNextStep: 处理网关节点，应调用网关处理器并按其结果流转")
  void shouldProcessGatewayAndMove_forGatewayNode() {
    // Arrange
    ProcessNode gatewayNode = createTestNode(NodeType.GATEWAY, null, ruleChainId); // 即使是网关，构造时也需一个非空rcId
    ProcessChain processChain = createTestProcessChain(gatewayNode);
    ProcessNodeId decidedPathId = ProcessNodeId.of("path-decided");

    when(instance.getCurrentNodeId()).thenReturn(currentNodeId);
    when(instance.getContext()).thenReturn(originalContext);
    when(processChainRepository.findById(chainId)).thenReturn(Optional.of(processChain));
    when(gatewayProcessor.process(gatewayNode, originalContext)).thenReturn(decidedPathId);

    // Act
    orchestrationService.executeNextStep(instance);

    // Assert
    verify(gatewayProcessor).process(gatewayNode, originalContext);
    verify(instance).moveToNextNode(decidedPathId, originalContext);
  }

  @Test
  @DisplayName("executeNextStep: 当找不到流程定义时，应抛出 IllegalStateException")
  void shouldThrowException_whenProcessDefinitionNotFound() {
    // Arrange
    when(processChainRepository.findById(chainId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> orchestrationService.executeNextStep(instance))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Cannot find process definition");

    verify(instance, never()).getCurrentNodeId(); // 验证此测试路径未调用多余的 stub
  }

  // --- Helper Methods ---
  private ProcessNode createTestNode(NodeType type, ProcessNodeId nextNode, com.zornflow.domain.rule.types.RuleChainId rcId) {
    return ProcessNode.builder()
      .id(currentNodeId)
      .type(type)
      .nextNodeId(nextNode)
      .ruleChainId(rcId)
      .properties(Collections.emptyMap())
      .conditions(Collections.emptyList())
      .build();
  }

  private ProcessChain createTestProcessChain(ProcessNode node) {
    return ProcessChain.builder()
      .id(chainId)
      .nodes(List.of(node))
      .build();
  }
}
