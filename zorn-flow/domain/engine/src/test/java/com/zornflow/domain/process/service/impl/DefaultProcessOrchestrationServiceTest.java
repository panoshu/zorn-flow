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
import com.zornflow.domain.rule.service.impl.DefaultRuleChainExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultProcessOrchestrationServiceTest {

  @Mock
  private ProcessChainRepository definitionRepository;
  @Mock
  private RuleChainRepository ruleChainRepository;
  @Mock
  private DefaultRuleChainExecutionService ruleChainExecutionService;
  @Mock
  private GatewayProcessor gatewayProcessor;
  @Mock
  private ProcessInstance processInstance;
  @Mock
  private ProcessChain processChain;
  @Mock
  private RuleChain ruleChain;

  @InjectMocks
  private DefaultProcessOrchestrationService orchestrationService;

  private final ProcessChainId processChainId = ProcessChainId.of("proc-chain-1");
  private final ProcessNodeId startNodeId = ProcessNodeId.of("start-node");
  private final ProcessNodeId nextNodeId = ProcessNodeId.of("next-node");
  private final com.zornflow.domain.rule.types.RuleChainId ruleChainId = com.zornflow.domain.rule.types.RuleChainId.of("rule-chain-1");
  private BusinessContext initialContext;
  private BusinessContext updatedContext;

  @BeforeEach
  void setUp() {
    initialContext = new BusinessContext(Map.of("status", "initial"));
    updatedContext = new BusinessContext(Map.of("status", "updated"));
  }

  @Test
  @DisplayName("Should execute rule chain for a business node and move to the next node")
  void executeNextStep_forBusinessNode_shouldExecuteRulesAndMove() {
    // Arrange
    ProcessNode businessNode = ProcessNode.builder()
      .id(startNodeId)
      .type(NodeType.BUSINESS)
      .ruleChainId(ruleChainId)
      .nextNodeId(nextNodeId)
      .properties(Collections.emptyMap()) // FIX: Provide required empty collections
      .conditions(Collections.emptyList()) // FIX: Provide required empty collections
      .build();

    when(processInstance.getProcessChainId()).thenReturn(processChainId);
    when(processInstance.getCurrentNodeId()).thenReturn(startNodeId);
    when(processInstance.getContext()).thenReturn(initialContext);
    when(definitionRepository.findById(processChainId)).thenReturn(Optional.of(processChain));
    when(processChain.getNodeById(startNodeId)).thenReturn(businessNode);
    when(ruleChainRepository.findById(ruleChainId)).thenReturn(Optional.of(ruleChain));
    when(ruleChainExecutionService.execute(ruleChain, initialContext)).thenReturn(updatedContext);

    // Act
    orchestrationService.executeNextStep(processInstance);

    // Assert
    verify(ruleChainExecutionService).execute(ruleChain, initialContext);
    verify(processInstance).moveToNextNode(nextNodeId, updatedContext);
  }

  @Test
  @DisplayName("Should process gateway node and move to the decided next node")
  void executeNextStep_forGatewayNode_shouldProcessGatewayAndMove() {
    // Arrange
    ProcessNode gatewayNode = ProcessNode.builder()
      .id(startNodeId)
      .type(NodeType.GATEWAY)
      .ruleChainId(ruleChainId)
      .properties(Collections.emptyMap()) // FIX: Provide required empty collections
      .conditions(Collections.emptyList()) // FIX: Provide required empty collections
      .build();
    ProcessNodeId decidedNextNodeId = ProcessNodeId.of("path-b");

    when(processInstance.getProcessChainId()).thenReturn(processChainId);
    when(processInstance.getCurrentNodeId()).thenReturn(startNodeId);
    when(processInstance.getContext()).thenReturn(initialContext);
    when(definitionRepository.findById(processChainId)).thenReturn(Optional.of(processChain));
    when(processChain.getNodeById(startNodeId)).thenReturn(gatewayNode);
    when(gatewayProcessor.process(gatewayNode, initialContext)).thenReturn(decidedNextNodeId);

    // Act
    orchestrationService.executeNextStep(processInstance);

    // Assert
    verify(gatewayProcessor).process(gatewayNode, initialContext);
    verify(processInstance).moveToNextNode(decidedNextNodeId, initialContext);
  }

  @Test
  @DisplayName("Should throw IllegalStateException if process definition is not found")
  void executeNextStep_whenProcessDefinitionNotFound_shouldThrowException() {
    // Arrange
    when(processInstance.getProcessChainId()).thenReturn(processChainId);
    when(definitionRepository.findById(processChainId)).thenReturn(Optional.empty());

    // Act & Assert
    var exception = assertThrows(IllegalStateException.class, () -> orchestrationService.executeNextStep(processInstance));

    assertTrue(exception.getMessage().contains("Cannot find process definition"));
  }

  @Test
  @DisplayName("Should move to next node without executing rules if business node has no rule chain")
  void executeNextStep_forBusinessNodeWithNoRules_shouldMoveWithoutExecuting() {
    // Arrange
    ProcessNode businessNode = mock(ProcessNode.class);
    when(businessNode.getType()).thenReturn(NodeType.BUSINESS);
    when(businessNode.getNextNodeId()).thenReturn(nextNodeId);
    when(businessNode.getRuleChainId()).thenReturn(null); // Explicitly return null

    when(processInstance.getProcessChainId()).thenReturn(processChainId);
    when(processInstance.getCurrentNodeId()).thenReturn(startNodeId);
    when(processInstance.getContext()).thenReturn(initialContext);
    when(definitionRepository.findById(processChainId)).thenReturn(Optional.of(processChain));
    when(processChain.getNodeById(startNodeId)).thenReturn(businessNode);

    // Act
    orchestrationService.executeNextStep(processInstance);

    // Assert
    verify(ruleChainExecutionService, never()).execute(any(), any());
    verify(processInstance).moveToNextNode(nextNodeId, initialContext);
  }

  @Test
  @DisplayName("Should throw IllegalStateException if rule chain definition is not found")
  void executeNextStep_whenRuleChainNotFound_shouldThrowException() {
    // Arrange
    ProcessNode businessNode = ProcessNode.builder()
      .id(startNodeId)
      .type(NodeType.BUSINESS)
      .ruleChainId(ruleChainId)
      .properties(Collections.emptyMap()) // FIX: Provide required empty collections
      .conditions(Collections.emptyList()) // FIX: Provide required empty collections
      .build();

    when(processInstance.getProcessChainId()).thenReturn(processChainId);
    when(processInstance.getCurrentNodeId()).thenReturn(startNodeId);
    when(processInstance.getContext()).thenReturn(initialContext);
    when(definitionRepository.findById(processChainId)).thenReturn(Optional.of(processChain));
    when(processChain.getNodeById(startNodeId)).thenReturn(businessNode);
    when(ruleChainRepository.findById(ruleChainId)).thenReturn(Optional.empty());

    // Act & Assert
    var exception = assertThrows(IllegalStateException.class, () -> orchestrationService.executeNextStep(processInstance));

    assertTrue(exception.getMessage().contains("RuleChainDefinition not found"));
  }
}
