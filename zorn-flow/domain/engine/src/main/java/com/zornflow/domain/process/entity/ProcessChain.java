package com.zornflow.domain.process.entity;

import com.domain.contract.aggregate.AggregateRoot;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessChainName;
import com.zornflow.domain.process.types.ProcessNodeId;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
  private final ProcessNodeId startNodeId;

  /**
   * 私有构造函数
   */
  @Builder
  private ProcessChain(ProcessChainId id, ProcessChainName name, String description, List<ProcessNode> nodes) {
    super(Objects.requireNonNull(id, "流程ID不能为空"));
    this.name = name != null ? name : ProcessChainName.of(id);
    this.description = description != null ? description : "";
    this.nodes = buildNodesMap(nodes);
    this.startNodeId = getStartNodeId(nodes);
    validateInvariants();
  }

  /**
   * [新增] 从输入的节点列表构建一个有序的、可快速查找的 Map。
   * 这个方法封装了数据结构转换的实现细节。
   *
   * @param nodeList 必须是有序的、非空的节点列表
   * @return 一个保留了插入顺序的 LinkedHashMap
   */
  private Map<ProcessNodeId, ProcessNode> buildNodesMap(List<ProcessNode> nodeList) {
    if (nodeList == null || nodeList.isEmpty()) {
      throw new IllegalArgumentException("流程的节点列表不能为空！");
    }

    return nodeList.stream()
      .collect(Collectors.toMap(
        ProcessNode::getId,
        node -> node,
        (existing, replacement) -> {
          throw new IllegalArgumentException("发现重复的节点ID: " + existing.getId().value());
        },
        LinkedHashMap::new
      ));
  }

  /**
   * 设置起始节点
   *
   * @param nodeList 初始化节点列表
   * @throws IllegalArgumentException 如果节点ID不存在
   */
  private ProcessNodeId getStartNodeId(List<ProcessNode> nodeList) {
    ProcessNodeId nodeId = nodeList.getFirst().getId();
    Objects.requireNonNull(nodeId, "起始节点ID不能为空");
    if (!this.nodes.containsKey(nodeId)) {
      throw new IllegalArgumentException("起始节点ID不存在: " + nodeId.value());
    }
    return nodeId;
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
    return this.getNodeById(ProcessNodeId.of(nodeIdStr));
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
