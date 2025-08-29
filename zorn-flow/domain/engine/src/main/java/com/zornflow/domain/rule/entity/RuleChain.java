package com.zornflow.domain.rule.entity;

import com.domain.contract.aggregate.AggregateRoot;
import com.zornflow.domain.common.types.Version;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.domain.rule.types.RuleChainName;
import com.zornflow.domain.rule.types.RuleId;
import lombok.Builder;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 规则链聚合根
 * 包含多个规则，负责规则的管理和执行顺序
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 15:54
 */

@Getter
public class RuleChain extends AggregateRoot<RuleChainId> {
  private final RuleChainName name;
  private final Version version;
  private final String source;
  private final String description;
  private final List<Rule> rules;
  private final Map<RuleId, Rule> ruleIndex;

  @Builder
  private RuleChain(RuleChainId id, RuleChainName name, Version version, String source, String description, List<Rule> rules) {
    super(Objects.requireNonNull(id, "规则ID不能为空"));
    this.name = name != null ? name : RuleChainName.of(id);
    this.version = Objects.requireNonNull(version, "版本不能为空");
    this.source = source != null ? source : "";
    this.description = description != null ? description : "";
    this.rules = new ArrayList<>(rules);
    this.ruleIndex = createRuleIndex(this.rules);
    validateInvariants();
    sortRulesByPriority();
  }

  /**
   * 创建规则索引
   */
  private static Map<RuleId, Rule> createRuleIndex(List<Rule> rules) {
    return rules.stream()
      .collect(Collectors.toMap(Rule::getId, rule -> rule));
  }

  /**
   * 按优先级排序规则
   */
  private void sortRulesByPriority() {
    rules.sort(Comparator.comparing(Rule::getPriority));
  }

  /**
   * 添加规则到规则链
   *
   * @param rule 要添加的规则
   * @throws IllegalArgumentException 如果规则ID已存在
   */
  public void addRule(Rule rule) {
    Objects.requireNonNull(rule, "规则不能为空");
    if (ruleIndex.containsKey(rule.getId())) {
      throw new IllegalArgumentException("规则ID已存在: " + rule.getId().value());
    }
    rules.add(rule);
    ruleIndex.put(rule.getId(), rule);
    sortRulesByPriority();
  }

  /**
   * 从规则链中移除规则
   *
   * @param ruleId 规则ID
   * @return 被移除的规则，如果不存在则返回null
   */
  public Rule removeRule(RuleId ruleId) {
    Objects.requireNonNull(ruleId, "规则ID不能为空");
    Rule removed = ruleIndex.remove(ruleId);
    if (removed != null) {
      rules.remove(removed);
    }
    return removed;
  }

  /**
   * 获取规则链中的所有规则（按优先级排序）
   *
   * @return 不可修改的规则列表
   */
  public List<Rule> getRules() {
    return Collections.unmodifiableList(rules);
  }

  /**
   * 根据ID获取规则
   *
   * @param ruleId 规则ID
   * @return 规则的Optional
   */
  public Optional<Rule> getRuleById(RuleId ruleId) {
    return Optional.ofNullable(ruleIndex.get(ruleId));
  }

  /**
   * 判断规则链是否包含指定规则
   *
   * @param ruleId 规则ID
   * @return true表示包含该规则
   */
  public boolean containsRule(RuleId ruleId) {
    return ruleIndex.containsKey(ruleId);
  }

  /**
   * 获取规则数量
   *
   * @return 规则数量
   */
  public int getRuleCount() {
    return rules.size();
  }

  @Override
  protected void validateInvariants() {

  }
}
