package com.zornflow.domain.process.valueobject;

import com.ddd.contract.valueobject.BaseValueObject;
import com.zornflow.domain.common.context.FlowContext;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.domain.rule.valueobject.RuleChainExecutionResult;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * 节点执行结果值对象
 * 封装单个流程节点的执行结果信息
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 16:21
 */

@Getter
public class NodeExecutionResult implements BaseValueObject {
  private final ProcessNodeId nodeId;
  private final String nodeName;
  private final boolean success;
  private final Instant startTime;
  private final Instant endTime;
  private final RuleChainExecutionResult ruleChainResult;
  private final FlowContext contextAfterExecution;
  private final String message;
  private final Throwable error;

  /**
   * 创建成功的节点执行结果
   * @param nodeId 节点ID
   * @param nodeName 节点名称
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @param ruleChainResult 规则链执行结果
   * @param contextAfterExecution 执行后的上下文
   * @return 节点执行结果
   */
  public static NodeExecutionResult success(ProcessNodeId nodeId, String nodeName, Instant startTime,
                                            Instant endTime, RuleChainExecutionResult ruleChainResult,
                                            FlowContext contextAfterExecution) {
    return new NodeExecutionResult(nodeId, nodeName, true, startTime, endTime, ruleChainResult,
      contextAfterExecution, null, null);
  }

  /**
   * 创建失败的节点执行结果
   * @param nodeId 节点ID
   * @param nodeName 节点名称
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @param contextAfterExecution 执行后的上下文
   * @param error 错误信息
   * @return 节点执行结果
   */
  public static NodeExecutionResult failure(ProcessNodeId nodeId, String nodeName, Instant startTime,
                                            Instant endTime, FlowContext contextAfterExecution,
                                            Throwable error) {
    return new NodeExecutionResult(nodeId, nodeName, false, startTime, endTime, null,
      contextAfterExecution, error.getMessage(), error);
  }

  /**
   * 私有构造函数
   */
  private NodeExecutionResult(ProcessNodeId nodeId, String nodeName, boolean success, Instant startTime,
                              Instant endTime, RuleChainExecutionResult ruleChainResult,
                              FlowContext contextAfterExecution, String message, Throwable error) {
    this.nodeId = Objects.requireNonNull(nodeId, "节点ID不能为空");
    this.nodeName = nodeName != null ? nodeName : nodeId.value().value();
    this.success = success;
    this.startTime = Objects.requireNonNull(startTime, "开始时间不能为空");
    this.endTime = Objects.requireNonNull(endTime, "结束时间不能为空");
    this.ruleChainResult = ruleChainResult;
    this.contextAfterExecution = Objects.requireNonNull(contextAfterExecution, "执行后上下文不能为空");
    this.message = message;
    this.error = error;
  }

  /**
   * 计算执行耗时（毫秒）
   * @return 执行耗时
   */
  public long getExecutionDuration() {
    return java.time.Duration.between(startTime, endTime).toMillis();
  }

  /**
   * 判断是否有规则链执行结果
   * @return true表示有规则链执行结果
   */
  public boolean hasRuleChainResult() {
    return ruleChainResult != null;
  }
}
