package com.zornflow.domain.process.service;

import com.zornflow.domain.common.context.FlowContext;
import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.domain.process.valueobject.ProcessExecutionResult;

/**
 * 流程执行领域服务接口
 * 定义流程执行的核心操作
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/1 9:49
 */

public interface ProcessExecutionService {
  /**
   * 执行整个流程
   * @param processChain 要执行的流程
   * @param context 业务上下文
   * @return 流程执行结果
   */
  ProcessExecutionResult executeProcess(ProcessChain processChain, FlowContext context);

  /**
   * 从指定节点开始执行流程
   * @param processChain 要执行的流程
   * @param startNodeId 起始节点ID
   * @param context 业务上下文
   * @return 流程执行结果
   */
  ProcessExecutionResult executeProcessFromNode(ProcessChain processChain, ProcessNodeId startNodeId, FlowContext context);

  /**
   * 继续执行流程（用于中断后恢复）
   * @param processExecutionId 流程执行ID
   * @param context 最新业务上下文
   * @return 流程执行结果
   */
  ProcessExecutionResult continueProcess(String processExecutionId, FlowContext context);
}
