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
import com.zornflow.domain.rule.types.RuleChainId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultProcessOrchestrationService}.
 */
@ExtendWith(MockitoExtension.class)
class DefaultProcessOrchestrationServiceTest {

  // Mock all dependencies to isolate the service logic
  @Mock
  private ProcessChainRepository definitionRepository;
  @Mock
  private RuleChainRepository ruleChainRepository;
  @Mock
  private DefaultRuleChainExecutionService ruleChainExecutionService;
  @Mock
  private GatewayProcessor gatewayProcessor;
  @Mock
  private ProcessChain processChain;
  @Mock
  private RuleChain ruleChain;

  // Use @Spy on the real object to allow partial mocking and state verification
  @Spy
  private ProcessInstance processInstance;

  // The class under test
  @InjectMocks
  private DefaultProcessOrchestrationService orchestrationService;

  // Common test data
  private final ProcessChainId processChainId = ProcessChainId.of("proc-chain-1");
  private final ProcessNodeId startNodeId = ProcessNodeId.of("start-node");
  private final ProcessNodeId nextNodeId = ProcessNodeId.of("next-node");
  private final RuleChainId ruleChainId = RuleChainId.of("rule-chain-1");
  private BusinessContext initialContext;
  private BusinessContext updatedContext;


  @BeforeEach
  void setUp() {
    initialContext = new BusinessContext(Map.of("status", "initial"));
    updatedContext = new BusinessContext(Map.of("status", "updated"));

    // Create a spy of a real ProcessInstance object to track its state changes
    processInstance = spy(ProcessInstance.start(processChainId, initialContext, startNodeId));
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
      .build();

    // Mock repository and service calls
    when(definitionRepository.findById(processChainId)).thenReturn(Optional.of(processChain));
    when(processChain.getNodeById(startNodeId)).thenReturn(businessNode);
    when(ruleChainRepository.findById(ruleChainId)).thenReturn(Optional.of(ruleChain));
    when(ruleChainExecutionService.execute(ruleChain, initialContext)).thenReturn(updatedContext);

    // Act
    orchestrationService.executeNextStep(processInstance);

    // Assert
    // Verify that the rule chain was fetched and executed
    verify(ruleChainRepository).findById(ruleChainId);
    verify(ruleChainExecutionService).execute(ruleChain, initialContext);
    // Verify that the instance was moved to the next node with the UPDATED context
    verify(processInstance).moveToNextNode(nextNodeId, updatedContext);
  }

  @Test
  @DisplayName("Should move to next node without executing rules if business node has no rule chain")
  void executeNextStep_forBusinessNodeWithNoRules_shouldMoveWithoutExecuting() {
    // Arrange
    ProcessNode businessNode = ProcessNode.builder()
      .id(startNodeId)
      .type(NodeType.BUSINESS)
      // No ruleChainId
      .nextNodeId(nextNodeId)
      .build();

    when(definitionRepository.findById(processChainId)).thenReturn(Optional.of(processChain));
    when(processChain.getNodeById(startNodeId)).thenReturn(businessNode);

    // Act
    orchestrationService.executeNextStep(processInstance);

    // Assert
    // Verify that rule-related services were NOT called
    verify(ruleChainRepository, never()).findById(any());
    verify(ruleChainExecutionService, never()).execute(any(), any());
    // Verify that the instance was moved to the next node with the ORIGINAL context
    verify(processInstance).moveToNextNode(nextNodeId, initialContext);
  }

  @Test
  @DisplayName("Should process gateway node and move to the decided next node")
  void executeNextStep_forGatewayNode_shouldProcessGatewayAndMove() {
    // Arrange
    ProcessNode gatewayNode = ProcessNode.builder()
      .id(startNodeId)
      .type(NodeType.GATEWAY)
      // A gateway's next node is determined by the processor, not statically
      .build();

    ProcessNodeId decidedNextNodeId = ProcessNodeId.of("path-b");

    when(definitionRepository.findById(processChainId)).thenReturn(Optional.of(processChain));
    when(processChain.getNodeById(startNodeId)).thenReturn(gatewayNode);
    // Mock the gateway processor to return a specific next node
    when(gatewayProcessor.process(gatewayNode, initialContext)).thenReturn(decidedNextNodeId);

    // Act
    orchestrationService.executeNextStep(processInstance);

    // Assert
    // Verify that the gateway processor was called
    verify(gatewayProcessor).process(gatewayNode, initialContext);
    // Verify that rule execution services were NOT called
    verify(ruleChainExecutionService, never()).execute(any(), any());
    // Verify that the instance was moved to the node decided by the gateway
    verify(processInstance).moveToNextNode(decidedNextNodeId, initialContext);
  }

  @Test
  @DisplayName("Should throw IllegalStateException if process definition is not found")
  void executeNextStep_whenProcessDefinitionNotFound_shouldThrowException() {
    // Arrange
    when(definitionRepository.findById(processChainId)).thenReturn(Optional.empty());

    // Act & Assert
    var exception = assertThrows(IllegalStateException.class, () -> {
      orchestrationService.executeNextStep(processInstance);
    });

    assertTrue(exception.getMessage().contains("Cannot find process definition"));
  }

  @Test
  @DisplayName("Should throw IllegalStateException if rule chain definition is not found")
  void executeNextStep_whenRuleChainNotFound_shouldThrowException() {
    // Arrange
    ProcessNode businessNode = ProcessNode.builder()
      .id(startNodeId)
      .type(NodeType.BUSINESS)
      .ruleChainId(ruleChainId)
      .build();

    when(definitionRepository.findById(processChainId)).thenReturn(Optional.of(processChain));
    when(processChain.getNodeById(startNodeId)).thenReturn(businessNode);
    // Mock the repository to return an empty optional for the rule chain
    when(ruleChainRepository.findById(ruleChainId)).thenReturn(Optional.empty());

    // Act & Assert
    var exception = assertThrows(IllegalStateException.class, () -> {
      orchestrationService.executeNextStep(processInstance);
    });
    assertTrue(exception.getMessage().contains("RuleChainDefinition not found"));
  }
}
