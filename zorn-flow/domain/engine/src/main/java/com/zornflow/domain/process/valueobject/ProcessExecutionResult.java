package com.zornflow.domain.process.valueobject;

import com.zornflow.domain.common.context.FlowContext;
import com.zornflow.domain.process.types.ProcessNodeId;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 流程执行结果值对象
 * 封装流程执行的整体结果信息
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 17:10
 */

@Getter
public final class ProcessExecutionResult {
  private final String executionId;
  private final boolean success;
  private final FlowContext finalContext;
  private final ProcessNodeId endNodeId;
  private final List<NodeExecutionResult> nodeResults;
  private final String message;
  private final Throwable error;

  /**
   * 创建成功的流程执行结果
   * @param finalContext 最终业务上下文
   * @param endNodeId 结束节点ID
   * @param nodeResults 节点执行结果列表
   * @return 流程执行结果
   */
  public static ProcessExecutionResult success(FlowContext finalContext, ProcessNodeId endNodeId, List<NodeExecutionResult> nodeResults) {
    return new ProcessExecutionResult(
      generateExecutionId(), true, finalContext, endNodeId, nodeResults, null, null
    );
  }

  /**
   * 创建失败的流程执行结果
   * @param finalContext 最终业务上下文
   * @param currentNodeId 当前节点ID
   * @param nodeResults 节点执行结果列表
   * @param error 错误信息
   * @return 流程执行结果
   */
  public static ProcessExecutionResult failure(FlowContext finalContext, ProcessNodeId currentNodeId,
                                               List<NodeExecutionResult> nodeResults, Throwable error) {
    return new ProcessExecutionResult(
      generateExecutionId(), false, finalContext, currentNodeId, nodeResults, error.getMessage(), error
    );
  }

  /**
   * 生成流程执行ID
   */
  private static String generateExecutionId() {
    return "PROC-" + UUID.randomUUID().toString().substring(0, 18);
  }

  /**
   * 私有构造函数
   */
  private ProcessExecutionResult(String executionId, boolean success, FlowContext finalContext,
                                 ProcessNodeId endNodeId, List<NodeExecutionResult> nodeResults, String message, Throwable error) {
    this.executionId = Objects.requireNonNull(executionId, "执行ID不能为空");
    this.success = success;
    this.finalContext = Objects.requireNonNull(finalContext, "最终业务上下文不能为空");
    this.endNodeId = endNodeId;
    this.nodeResults = nodeResults != null ? Collections.unmodifiableList(nodeResults) : Collections.emptyList();
    this.message = message;
    this.error = error;
  }

  /**
   * 判断流程是否执行完成
   * @return true表示流程已完成
   */
  public boolean isCompleted() {
    return success && endNodeId != null;
  }

  /**
   * 获取第一个失败的节点执行结果
   * @return 失败的节点执行结果，如果没有则返回null
   */
  public NodeExecutionResult getFirstFailureNodeResult() {
    return nodeResults.stream()
      .filter(result -> !result.isSuccess())
      .findFirst()
      .orElse(null);
  }
}
