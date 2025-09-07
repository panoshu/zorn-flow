package com.zornflow.domain.rule.entity;

import com.zornflow.domain.rule.types.HandlerType;
import com.zornflow.domain.rule.types.Priority;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.domain.rule.types.RuleId;
import com.zornflow.domain.rule.valueobject.Handler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RuleChain 聚合根单元测试 (重构后)")
class RuleChainTest {

  private Rule rule_p10;
  private Rule rule_p20;
  private Rule rule_p05;
  private Rule rule_p15;

  @BeforeEach
  void setUp() {
    rule_p05 = createTestRule("rule5", 5);
    rule_p10 = createTestRule("rule10", 10);
    rule_p15 = createTestRule("rule15", 15);
    rule_p20 = createTestRule("rule20", 20);
  }

  private Rule createTestRule(String id, int priority) {
    return Rule.builder()
      .id(RuleId.of(id))
      .priority(Priority.of(priority))
      .handler(Handler.of(HandlerType.CLASS, "handler-" + id))
      .build();
  }

  @Test
  @DisplayName("构造器: 当规则列表乱序时，应能正确按优先级排序")
  void constructor_shouldSortRulesByPriority() {
    // Arrange
    List<Rule> rules = List.of(rule_p10, rule_p20, rule_p05, rule_p15);

    // Act
    RuleChain ruleChain = RuleChain.builder()
      .id(RuleChainId.of("test-chain"))
      .rules(rules)
      .build();

    // Assert
    assertThat(ruleChain.getRules()).containsExactly(rule_p05, rule_p10, rule_p15, rule_p20);
  }

  @Test
  @DisplayName("addRule: 添加一个最高优先级的规则，应插入到列表开头")
  void addRule_shouldInsertHighestPriorityAtBeginning() {
    // Arrange
    RuleChain ruleChain = RuleChain.builder()
      .id(RuleChainId.of("test-chain"))
      .rules(new ArrayList<>(List.of(rule_p10, rule_p20))) // 初始: 10, 20
      .build();

    // Act
    ruleChain.addRule(rule_p05); // 添加 5

    // Assert
    assertThat(ruleChain.getRules()).containsExactly(rule_p05, rule_p10, rule_p20);
  }

  @Test
  @DisplayName("addRule: 添加一个中间优先级的规则，应插入到列表中间")
  void addRule_shouldInsertMiddlePriorityInTheMiddle() {
    // Arrange
    RuleChain ruleChain = RuleChain.builder()
      .id(RuleChainId.of("test-chain"))
      .rules(new ArrayList<>(List.of(rule_p10, rule_p20))) // 初始: 10, 20
      .build();

    // Act
    ruleChain.addRule(rule_p15); // 添加 15

    // Assert
    assertThat(ruleChain.getRules()).containsExactly(rule_p10, rule_p15, rule_p20);
  }

  @Test
  @DisplayName("addRule: 添加一个最低优先级的规则，应插入到列表末尾")
  void addRule_shouldInsertLowestPriorityAtEnd() {
    // Arrange
    RuleChain ruleChain = RuleChain.builder()
      .id(RuleChainId.of("test-chain"))
      .rules(new ArrayList<>(List.of(rule_p05, rule_p10))) // 初始: 5, 10
      .build();

    // Act
    ruleChain.addRule(rule_p20); // 添加 20

    // Assert
    assertThat(ruleChain.getRules()).containsExactly(rule_p05, rule_p10, rule_p20);
  }

  @Test
  @DisplayName("addRule: 添加重复ID的规则时，应抛出 IllegalArgumentException")
  void addRule_shouldThrowException_whenRuleIdExists() {
    // Arrange
    RuleChain ruleChain = RuleChain.builder()
      .id(RuleChainId.of("test-chain"))
      .rules(new ArrayList<>(List.of(rule_p10)))
      .build();
    Rule duplicateRule = createTestRule("rule10", 99);

    // Act & Assert
    assertThatThrownBy(() -> ruleChain.addRule(duplicateRule))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("规则ID已存在: rule10");
  }

  @Test
  @DisplayName("removeRule: 应能成功移除一个规则并保持列表有序")
  void removeRule_shouldRemoveRuleSuccessfully() {
    // Arrange
    RuleChain ruleChain = RuleChain.builder()
      .id(RuleChainId.of("test-chain"))
      .rules(new ArrayList<>(List.of(rule_p05, rule_p10, rule_p20)))
      .build();

    // Act
    Rule removedRule = ruleChain.removeRule(rule_p10.getId());

    // Assert
    assertThat(removedRule).isEqualTo(rule_p10);
    assertThat(ruleChain.getRuleCount()).isEqualTo(2);
    assertThat(ruleChain.containsRule(rule_p10.getId())).isFalse();
    assertThat(ruleChain.getRules()).containsExactly(rule_p05, rule_p20);
  }
}
