package com.zornflow.domain.process.entity;

import com.domain.contract.aggregate.AggregateRoot;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessChainName;
import com.zornflow.domain.process.types.ProcessNodeId;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 流程聚合根
 * 包含多个流程节点，负责流程的整体管理和执行
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 17:18
 */

@Getter
public class ProcessChain extends AggregateRoot<ProcessChainId> {
  private final ProcessChainName name;
  private final String description;
  private final Map<ProcessNodeId, ProcessNode> nodes;
  private final Map<String, ProcessNode> nodeIdIndex;
  private ProcessNodeId startNodeId;

  /**
   * 私有构造函数
   */
  @Builder
  private ProcessChain(ProcessChainId id, ProcessChainName name, String description, List<ProcessNode> nodes) {
    super(Objects.requireNonNull(id, "流程ID不能为空"));
    this.name = name != null ? name : ProcessChainName.of(id);
    this.description = description != null ? description : "";
    this.nodes = new HashMap<>();
    this.nodeIdIndex = new HashMap<>();
    addNodes(nodes);
    determineStartNode();
    validateInvariants();
  }

  /**
   * 添加多个节点
   */
  private void addNodes(List<ProcessNode> nodes) {
    if (nodes == null || nodes.isEmpty()){
      throw new IllegalArgumentException("nodes cannot be null or empty");
    }

    for (ProcessNode node : nodes) {
      addNode(node);
    }
  }

  /**
   * 添加单个节点
   */
  public void addNode(ProcessNode node) {
    Objects.requireNonNull(node, "流程节点不能为空");
    ProcessNodeId nodeId = node.getId();
    String nodeIdStr = nodeId.value();

    if (nodes.containsKey(nodeId)) {
      throw new IllegalArgumentException("流程节点ID已存在: " + nodeIdStr);
    }
    if (nodeIdIndex.containsKey(nodeIdStr)) {
      throw new IllegalArgumentException("流程节点ID字符串已存在: " + nodeIdStr);
    }

    nodes.put(nodeId, node);
    nodeIdIndex.put(nodeIdStr, node);
  }

  /**
   * 确定流程的起始节点
   * 简单实现：取第一个添加的节点作为起始节点
   */
  private void determineStartNode() {
    if (!nodes.isEmpty() && startNodeId == null) {
      startNodeId = nodes.keySet().iterator().next();
    }
  }

  /**
   * 设置起始节点
   *
   * @param startNodeId 起始节点ID
   * @throws IllegalArgumentException 如果节点ID不存在
   */
  public void setStartNodeId(ProcessNodeId startNodeId) {
    Objects.requireNonNull(startNodeId, "起始节点ID不能为空");
    if (!nodes.containsKey(startNodeId)) {
      throw new IllegalArgumentException("起始节点ID不存在: " + startNodeId.value());
    }
    this.startNodeId = startNodeId;
  }

  /**
   * 根据ID获取节点
   *
   * @param nodeId 节点ID
   * @return 节点实例
   * @throws IllegalArgumentException 如果节点不存在
   */
  public ProcessNode getNodeById(ProcessNodeId nodeId) {
    ProcessNode node = nodes.get(nodeId);
    if (node == null) {
      throw new IllegalArgumentException("节点ID不存在: " + nodeId.value());
    }
    return node;
  }

  /**
   * 根据ID字符串获取节点
   *
   * @param nodeIdStr 节点ID字符串
   * @return 节点实例
   * @throws IllegalArgumentException 如果节点不存在
   */
  public ProcessNode getNodeByStringId(String nodeIdStr) {
    ProcessNode node = nodeIdIndex.get(nodeIdStr);
    if (node == null) {
      throw new IllegalArgumentException("节点ID不存在: " + nodeIdStr);
    }
    return node;
  }

  /**
   * 获取所有节点
   *
   * @return 不可修改的节点列表
   */
  public List<ProcessNode> getAllNodes() {
    return List.copyOf(nodes.values());
  }

  public boolean hasStartNode() {
    return startNodeId != null;
  }

  @Override
  protected void validateInvariants() {

  }

  @Override
  public Integer getVersion() {
    return super.getVersion();
  }

  @Override
  public Instant getCreatedAt() {
    return super.getCreatedAt();
  }

  @Override
  public Instant getUpdatedAt() {
    return super.getUpdatedAt();
  }
}
