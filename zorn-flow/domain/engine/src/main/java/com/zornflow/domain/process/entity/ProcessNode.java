package com.zornflow.domain.process.entity;

import com.domain.contract.aggregate.Entity;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.domain.process.types.ProcessNodeName;
import com.zornflow.domain.process.valueobject.GatewayCondition;
import com.zornflow.domain.process.valueobject.NodeType;
import com.zornflow.domain.rule.types.RuleChainId;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 17:12
 */

@Getter
public class ProcessNode extends Entity<ProcessNodeId> {
  private final ProcessNodeName name;
  private final NodeType type;
  private final Map<String, Object> properties;
  private final List<GatewayCondition> conditions;
  private final RuleChainId ruleChainId;
  private ProcessNodeId nextNodeId;

  /**
   * 私有构造函数
   */
  @Builder
  private ProcessNode(ProcessNodeId id, ProcessNodeName name, NodeType type, RuleChainId ruleChainId,
                      Map<String, Object> properties, List<GatewayCondition> conditions, ProcessNodeId nextNodeId) {
    super(Objects.requireNonNull(id, "流程节点ID不能为空"));
    this.name = name != null ? name : ProcessNodeName.of(id);
    this.type = Objects.requireNonNull(type, "节点类型不能为空");
    this.properties = Objects.requireNonNullElse(Map.copyOf(properties), Map.of());
    this.conditions = Objects.requireNonNullElse(List.copyOf(conditions), List.of());
    this.ruleChainId = Objects.requireNonNull(ruleChainId, "Rule Chain Id could not be null");
    this.nextNodeId = nextNodeId;
    validateInvariants();
  }

  /**
   * 更新下一个节点ID
   *
   * @param nextNodeId 下一个节点ID
   */
  public void updateNextNodeId(ProcessNodeId nextNodeId) {
    this.nextNodeId = nextNodeId;
  }

  /**
   * 判断是否有下一个节点
   *
   * @return true表示有下一个节点
   */
  public boolean hasNextNode() {
    return nextNodeId != null;
  }

  /**
   * 判断是否包含指定属性
   *
   * @param propertyName 属性名称
   * @return true表示包含该属性
   */
  public boolean hasProperty(String propertyName) {
    return properties.containsKey(propertyName);
  }

  /**
   * 获取属性值
   *
   * @param propertyName 属性名称
   * @return 属性值，如果不存在返回null
   */
  @SuppressWarnings("unchecked")
  public <T> T getProperty(String propertyName) {
    return (T) properties.get(propertyName);
  }

  @Override
  protected void validateInvariants() {

  }
}
