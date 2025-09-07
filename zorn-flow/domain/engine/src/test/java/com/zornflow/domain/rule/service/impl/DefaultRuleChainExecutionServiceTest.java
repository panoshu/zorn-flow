package com.zornflow.domain.rule.service.impl;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultRuleChainExecutionService 领域服务单元测试")
class DefaultRuleChainExecutionServiceTest {

  @Mock
  private ConditionEvaluator conditionEvaluator;

  @Mock
  private HandlerExecutorFactory handlerExecutorFactory;

  @Mock
  private HandlerExecutor handlerExecutor;

  // 被测试的类
  @InjectMocks
  private DefaultRuleChainExecutionService executionService;

  private Rule rule_p10;
  private Rule rule_p20;
  private BusinessContext context;

  @BeforeEach
  void setUp() {
    context = new BusinessContext(Map.of("amount", 100));
    rule_p10 = Rule.builder().id(RuleId.of("r10")).priority(Priority.of(10)).condition(Condition.of("#{true}")).handler(Handler.of(HandlerType.CLASS, "h10")).build();
    rule_p20 = Rule.builder().id(RuleId.of("r20")).priority(Priority.of(20)).condition(Condition.of("#{false}")).handler(Handler.of(HandlerType.CLASS, "h20")).build();
  }

  @Test
  @DisplayName("execute: 应按规则优先级顺序执行，并只执行条件为真的规则")
  void execute_shouldProcessRulesInOrderAndOnlyWhenConditionIsTrue() {
    // Arrange
    RuleChain ruleChain = RuleChain.builder()
      .id(RuleChainId.of("test-chain"))
      .rules(List.of(rule_p20, rule_p10)) // 故意乱序
      .build();

    // 模拟 rule_p10 的条件为真，rule_p20 的条件为假
    when(conditionEvaluator.evaluate(rule_p10.getCondition(), context)).thenReturn(true);
    when(conditionEvaluator.evaluate(rule_p20.getCondition(), context)).thenReturn(false);

    // 模拟能为 rule_p10 找到执行器
    when(handlerExecutorFactory.getExecutor(rule_p10.getHandler())).thenReturn(Optional.of(handlerExecutor));

    // Act
    BusinessContext finalContext = executionService.execute(ruleChain, context);

    // Assert
    // 1. 验证交互顺序
    var inOrder = inOrder(conditionEvaluator, handlerExecutorFactory, handlerExecutor);
    // 首先检查更高优先级的 rule_p10
    inOrder.verify(conditionEvaluator).evaluate(rule_p10.getCondition(), context);
    inOrder.verify(handlerExecutorFactory).getExecutor(rule_p10.getHandler());
    inOrder.verify(handlerExecutor).execute(rule_p10.getHandler(), context);
    // 然后检查较低优先级的 rule_p20
    inOrder.verify(conditionEvaluator).evaluate(rule_p20.getCondition(), context);

    // 2. 验证 rule_p20 的执行器从未被调用
    verify(handlerExecutorFactory, never()).getExecutor(rule_p20.getHandler());

    // 3. 验证返回的上下文是原始上下文（因为我们的 mock 没有修改它）
    assertThat(finalContext).isSameAs(context);
  }

  @Test
  @DisplayName("execute: 当找不到处理器时，应抛出 IllegalStateException")
  void execute_shouldThrowException_whenExecutorNotFound() {
    // Arrange
    RuleChain ruleChain = RuleChain.builder().id(RuleChainId.of("chain")).rules(List.of(rule_p10)).build();
    when(conditionEvaluator.evaluate(rule_p10.getCondition(), context)).thenReturn(true);
    // 模拟找不到执行器
    when(handlerExecutorFactory.getExecutor(rule_p10.getHandler())).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> executionService.execute(ruleChain, context))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("No handler executor found for type: CLASS");
  }
}
