package com.zornflow.domain.process.entity;

import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.domain.process.valueobject.NodeType;
import com.zornflow.domain.rule.types.RuleChainId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProcessChain 聚合根单元测试 (重构后)")
class ProcessChainTest {

  private ProcessNode node1;
  private ProcessNode node2;
  private ProcessNode node3;

  @BeforeEach
  void setUp() {
    node1 = createTestNode("node1");
    node2 = createTestNode("node2");
    node3 = createTestNode("node3");
  }

  private ProcessNode createTestNode(String id) {
    return ProcessNode.builder()
      .id(ProcessNodeId.of(id))
      .type(NodeType.BUSINESS)
      .ruleChainId(RuleChainId.of("rc-" + id))
      .properties(Collections.emptyMap())
      .conditions(Collections.emptyList())
      .build();
  }

  @Test
  @DisplayName("构造器: 当节点列表为 null 或空时，应抛出 IllegalArgumentException")
  void constructor_shouldThrowException_whenNodeListIsNullOrEmpty() {
    ProcessChainId chainId = ProcessChainId.of("pc1");

    assertThatThrownBy(() -> ProcessChain.builder().id(chainId).nodes(null).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("流程的节点列表不能为空！");

    assertThatThrownBy(() -> ProcessChain.builder().id(chainId).nodes(Collections.emptyList()).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("流程的节点列表不能为空！");
  }

  @Test
  @DisplayName("构造器: 应将传入列表的第一个节点正确设置为起始节点")
  void constructor_shouldCorrectlySetStartNodeBasedOnListOrder() {
    // Arrange
    List<ProcessNode> nodesInOrder = List.of(node2, node1, node3); // 明确 node2 是第一个

    // Act
    ProcessChain processChain = ProcessChain.builder()
      .id(ProcessChainId.of("pc1"))
      .nodes(nodesInOrder)
      .build();

    // Assert
    assertTrue(processChain.hasStartNode());
    assertEquals(node2.getId(), processChain.getStartNodeId(), "起始节点应为列表的第一个元素");
  }

  @Test
  @DisplayName("构造器: 当节点列表中存在重复ID时，应抛出 IllegalArgumentException")
  void constructor_shouldThrowException_whenDuplicateNodeIdExists() {
    // Arrange
    ProcessNode duplicateNode = createTestNode("node1");
    List<ProcessNode> nodesWithDuplicate = List.of(node1, node2, duplicateNode);

    // Act & Assert
    assertThatThrownBy(() -> ProcessChain.builder().id(ProcessChainId.of("pc1")).nodes(nodesWithDuplicate).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("发现重复的节点ID: node1");
  }

  @Test
  @DisplayName("getAllNodes: 返回的节点列表应与构造时传入的顺序一致")
  void getAllNodes_shouldReturnNodesInOriginalOrder() {
    // Arrange
    List<ProcessNode> nodesInOrder = List.of(node2, node1, node3);

    // Act
    ProcessChain processChain = ProcessChain.builder()
      .id(ProcessChainId.of("pc1"))
      .nodes(nodesInOrder)
      .build();
    List<ProcessNode> allNodes = processChain.getAllNodes();

    // Assert
    assertThat(allNodes).containsExactly(node2, node1, node3);
  }

  @Test
  @DisplayName("getNodeByStringId: 应能通过字符串ID成功查找到节点")
  void getNodeByStringId_shouldFindNodeSuccessfully() {
    // Arrange
    ProcessChain processChain = ProcessChain.builder()
      .id(ProcessChainId.of("pc1"))
      .nodes(List.of(node1, node2))
      .build();

    // Act
    ProcessNode foundNode = processChain.getNodeByStringId("node2");

    // Assert
    assertThat(foundNode).isEqualTo(node2);
    assertThatThrownBy(() -> processChain.getNodeByStringId("non-existent-id"))
      .isInstanceOf(IllegalArgumentException.class);
  }
}
