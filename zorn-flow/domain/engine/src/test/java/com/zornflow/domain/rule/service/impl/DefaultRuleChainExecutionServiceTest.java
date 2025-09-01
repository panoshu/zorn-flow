package com.zornflow.domain.rule.service.impl;

import com.zornflow.domain.common.types.Version;
import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.rule.entity.Rule;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.service.ConditionEvaluator;
import com.zornflow.domain.rule.service.HandlerExecutor;
import com.zornflow.domain.rule.service.HandlerExecutorFactory;
import com.zornflow.domain.rule.types.*;
import com.zornflow.domain.rule.valueobject.Handler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultRuleChainExecutionService}.
 */
@ExtendWith(MockitoExtension.class)
class DefaultRuleChainExecutionServiceTest {

  // Mock dependencies, as we only want to test the service's logic
  @Mock
  private ConditionEvaluator conditionEvaluator;
  @Mock
  private HandlerExecutorFactory handlerExecutorFactory;
  @Mock
  private HandlerExecutor handlerExecutor;

  // The class we are testing
  @InjectMocks
  private DefaultRuleChainExecutionService executionService;

  private BusinessContext initialContext;
  private Rule rule1_priority10;
  private Rule rule2_priority20;
  private Rule rule3_priority5; // Highest priority

  @BeforeEach
  void setUp() {
    // Prepare common test data before each test
    initialContext = new BusinessContext(Map.of("amount", 100));

    // Create some rules with different priorities
    rule1_priority10 = Rule.builder()
      .id(RuleId.of("rule-1"))
      .name(RuleName.of("Rule 1"))
      .priority(Priority.of(10))
      .condition(Condition.of("#{amount > 50}"))
      .handler(Handler.of(HandlerType.CLASS, "handler1"))
      .build();

    rule2_priority20 = Rule.builder()
      .id(RuleId.of("rule-2"))
      .name(RuleName.of("Rule 2"))
      .priority(Priority.of(20)) // Lower priority than rule1
      .condition(Condition.of("#{true}"))
      .handler(Handler.of(HandlerType.CLASS, "handler2"))
      .build();

    rule3_priority5 = Rule.builder()
      .id(RuleId.of("rule-3"))
      .name(RuleName.of("Rule 3"))
      .priority(Priority.of(5)) // Highest priority
      .condition(Condition.of("#{false}"))
      .handler(Handler.of(HandlerType.CLASS, "handler3"))
      .build();
  }

  @Test
  @DisplayName("Should execute rules in correct priority order")
  void execute_shouldProcessRulesInPriorityOrder() {
    // Arrange: Create a rule chain with rules in a jumbled order
    RuleChain ruleChain = RuleChain.builder()
      .id(RuleChainId.of("test-chain"))
      .version(Version.defaultVersion())
      .rules(List.of(rule1_priority10, rule2_priority20, rule3_priority5))
      .build();

    // Mock the behavior of dependencies
    // All conditions are true, so all handlers should be executed
    when(conditionEvaluator.evaluate(any(Condition.class), any(BusinessContext.class))).thenReturn(true);
    when(handlerExecutorFactory.getExecutor(any(Handler.class))).thenReturn(Optional.of(handlerExecutor));

    // Act: Execute the service method
    executionService.execute(ruleChain, initialContext);

    // Assert: Verify that the handlers were called in the correct order (5, 10, 20)
    var inOrder = inOrder(conditionEvaluator, handlerExecutor);
    inOrder.verify(conditionEvaluator).evaluate(rule3_priority5.getCondition(), initialContext);
    inOrder.verify(handlerExecutor).execute(rule3_priority5.getHandler(), initialContext);

    inOrder.verify(conditionEvaluator).evaluate(rule1_priority10.getCondition(), initialContext);
    inOrder.verify(handlerExecutor).execute(rule1_priority10.getHandler(), initialContext);

    inOrder.verify(conditionEvaluator).evaluate(rule2_priority20.getCondition(), initialContext);
    inOrder.verify(handlerExecutor).execute(rule2_priority20.getHandler(), initialContext);
  }

  @Test
  @DisplayName("Should only execute handler when condition is true")
  void execute_shouldOnlyExecuteHandlerWhenConditionIsTrue() {
    // Arrange
    RuleChain ruleChain = RuleChain.builder()
      .id(RuleChainId.of("test-chain"))
      .version(Version.defaultVersion())
      .rules(List.of(rule1_priority10, rule3_priority5))
      .build();

    // Mock: rule3's condition is true, rule1's is false
    when(conditionEvaluator.evaluate(rule3_priority5.getCondition(), initialContext)).thenReturn(true);
    when(conditionEvaluator.evaluate(rule1_priority10.getCondition(), initialContext)).thenReturn(false);
    when(handlerExecutorFactory.getExecutor(rule3_priority5.getHandler())).thenReturn(Optional.of(handlerExecutor));

    // Act
    executionService.execute(ruleChain, initialContext);

    // Assert
    // Verify handler for rule3 was called because its condition was true
    verify(handlerExecutor, times(1)).execute(rule3_priority5.getHandler(), initialContext);
    // Verify handler for rule1 was NEVER called
    verify(handlerExecutor, never()).execute(rule1_priority10.getHandler(), initialContext);
  }

  @Test
  @DisplayName("Should throw exception if handler executor is not found")
  void execute_shouldThrowExceptionWhenExecutorNotFound() {
    // Arrange
    RuleChain ruleChain = RuleChain.builder()
      .id(RuleChainId.of("test-chain"))
      .version(Version.defaultVersion())
      .rules(List.of(rule1_priority10))
      .build();

    when(conditionEvaluator.evaluate(any(), any())).thenReturn(true);
    // Mock: The factory returns an empty Optional, simulating no available executor
    when(handlerExecutorFactory.getExecutor(any())).thenReturn(Optional.empty());

    // Act & Assert
    // Verify that executing the chain throws an IllegalStateException
    var exception = assertThrows(IllegalStateException.class, () -> {
      executionService.execute(ruleChain, initialContext);
    });

    assertTrue(exception.getMessage().contains("No handler executor found for type"));
  }

  @Test
  @DisplayName("Should return the final context after execution")
  void execute_shouldReturnFinalContext() {
    // Arrange
    RuleChain ruleChain = RuleChain.builder()
      .id(RuleChainId.of("test-chain"))
      .version(Version.defaultVersion())
      .rules(List.of(rule1_priority10))
      .build();

    when(conditionEvaluator.evaluate(any(), any())).thenReturn(false); // No rule will execute

    // Act
    BusinessContext resultContext = executionService.execute(ruleChain, initialContext);

    // Assert
    // Since BusinessContext is immutable, it should be the same instance if not modified.
    assertSame(initialContext, resultContext, "Context should be returned unmodified if no handlers execute");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException for null arguments")
  void execute_shouldThrowExceptionForNulls() {
    assertThrows(IllegalArgumentException.class, () -> executionService.execute(null, initialContext));
    RuleChain ruleChain = RuleChain.builder().id(RuleChainId.of("chain")).rules(List.of(rule1_priority10)).version(Version.defaultVersion()).build();
    assertThrows(IllegalArgumentException.class, () -> executionService.execute(ruleChain, null));
  }
}
