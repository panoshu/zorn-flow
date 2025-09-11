package com.zornflow.domain.process.entity;

import com.zornflow.domain.common.types.identifier.DomainIds;
import com.zornflow.domain.common.types.identifier.MockUlidStrategy;
import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessInstanceId;
import com.zornflow.domain.process.types.ProcessNodeId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("ProcessInstance 聚合根单元测试")
class ProcessInstanceTest {

  private ProcessChainId processChainId;
  private ProcessNodeId startNodeId;
  private BusinessContext initialContext;

  @BeforeAll
  static void setUpBeforeAll() throws Exception {
    DomainIds.register(Map.of(
      ProcessInstanceId.class, new MockUlidStrategy()
    ));
  }

  @BeforeEach
  void setUp() {
    processChainId = ProcessChainId.of("proc-chain-1");
    startNodeId = ProcessNodeId.of("start-node");
    initialContext = new BusinessContext(Map.of("user", "test"));
  }

  @Test
  @DisplayName("start 工厂方法: 应能成功创建一个处于 RUNNING 状态的新实例")
  void start_shouldCreateNewInstanceInRunningState() {
    // Act
    ProcessInstance instance = ProcessInstance.start(processChainId, initialContext, startNodeId);

    // Assert
    assertAll("新创建的实例应具有正确的初始状态",
      () -> assertThat(instance.getId()).isNotNull(),
      () -> assertThat(instance.getProcessChainId()).isEqualTo(processChainId),
      // [修正] 移除了对 getStartNodeId() 的断言
      () -> assertThat(instance.getCurrentNodeId()).isEqualTo(startNodeId),
      () -> assertThat(instance.getContext()).isEqualTo(initialContext),
      () -> assertThat(instance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING)
    );
  }

  @Test
  @DisplayName("start 工厂方法: 当任何必要参数为 null 时，应抛出 IllegalArgumentException")
  void start_shouldThrowException_whenArgumentsAreNull() {
    // 使用 assertAll 将多个相关的断言组合在一起
    assertAll("启动流程实例的必要参数不能为空",
      () -> assertThatThrownBy(() -> ProcessInstance.start(null, initialContext, startNodeId))
        .isInstanceOf(IllegalArgumentException.class)
        .withFailMessage("当 processChainId 为 null 时应抛出异常"),

      () -> assertThatThrownBy(() -> ProcessInstance.start(processChainId, null, startNodeId))
        .isInstanceOf(IllegalArgumentException.class)
        .withFailMessage("当 initialContext 为 null 时应抛出异常"),

      () -> assertThatThrownBy(() -> ProcessInstance.start(processChainId, initialContext, null))
        .isInstanceOf(IllegalArgumentException.class)
        .withFailMessage("当 startNodeId 为 null 时应抛出异常")
    );
  }

  @Test
  @DisplayName("moveToNextNode: 应能更新当前节点ID和业务上下文")
  void moveToNextNode_shouldUpdateCurrentNodeAndContext() {
    // Arrange
    ProcessInstance instance = ProcessInstance.start(processChainId, initialContext, startNodeId);
    ProcessNodeId nextNodeId = ProcessNodeId.of("next-node");
    BusinessContext updatedContext = initialContext.with("status", "approved");

    // Act
    instance.moveToNextNode(nextNodeId, updatedContext);

    // Assert
    assertThat(instance.getCurrentNodeId()).isEqualTo(nextNodeId);
    assertThat(instance.getContext()).isEqualTo(updatedContext);
    assertThat(instance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);
  }

  @Test
  @DisplayName("moveToNextNode: 当下一个节点ID为 null 时，应将状态更新为 COMPLETED")
  void moveToNextNode_shouldCompleteInstance_whenNextNodeIsNull() {
    // Arrange
    ProcessInstance instance = ProcessInstance.start(processChainId, initialContext, startNodeId);

    // Act
    instance.moveToNextNode(null, initialContext);

    // Assert
    assertThat(instance.getCurrentNodeId()).isNull();
    assertThat(instance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.COMPLETED);
  }

  @Test
  @DisplayName("moveToNextNode: 当实例不处于 RUNNING 状态时，应抛出 IllegalStateException")
  void moveToNextNode_shouldThrowException_whenNotRunning() {
    // Arrange
    ProcessInstance instance = ProcessInstance.start(processChainId, initialContext, startNodeId);
    instance.fail(initialContext); // 将状态置为 FAILED

    // Act & Assert
    assertThat(instance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.FAILED);
    assertThatThrownBy(() -> instance.moveToNextNode(ProcessNodeId.of("some-node"), initialContext))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Process instance is not in a RUNNING state");
  }

  @Test
  @DisplayName("fail: 应能将状态更新为 FAILED 并更新上下文")
  void fail_shouldUpdateStatusToFailed() {
    // Arrange
    ProcessInstance instance = ProcessInstance.start(processChainId, initialContext, startNodeId);
    BusinessContext failureContext = initialContext.with("error", "something went wrong");

    // Act
    instance.fail(failureContext);

    // Assert
    assertThat(instance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.FAILED);
    assertThat(instance.getContext()).isEqualTo(failureContext);
  }

  @Test
  @DisplayName("fail: 当实例不处于 RUNNING 状态时，应抛出 IllegalStateException")
  void fail_shouldThrowException_whenNotRunning() {
    // Arrange
    ProcessInstance instance = ProcessInstance.start(processChainId, initialContext, startNodeId);
    instance.moveToNextNode(null, initialContext); // 将状态置为 COMPLETED

    // Act & Assert
    assertThat(instance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.COMPLETED);
    assertThatThrownBy(() -> instance.fail(initialContext))
      .isInstanceOf(IllegalStateException.class);
  }
}
