package com.zornflow.domain.process.entity;

import com.domain.contract.aggregate.AggregateRoot;
import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessInstanceId;
import com.zornflow.domain.process.types.ProcessNodeId;
import lombok.Getter;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 22:30
 **/

@Getter
public class ProcessInstance extends AggregateRoot<ProcessInstanceId> {
  private final ProcessChainId processChainId;
  private ProcessInstanceStatus status;
  private ProcessNodeId currentNodeId;
  private BusinessContext context;

  protected ProcessInstance(ProcessInstanceId processInstanceId, ProcessChainId processChainId,
                            BusinessContext initialContext, ProcessNodeId startNodeId) {
    super(processInstanceId);
    this.processChainId = processChainId;
    this.context = initialContext;
    this.currentNodeId = startNodeId;
  }

  public static ProcessInstance start(ProcessChainId definitionId, BusinessContext initialContext, ProcessNodeId startNodeId) {
    if (definitionId == null || initialContext == null || startNodeId == null) {
      throw new IllegalArgumentException("ProcessChainId, InitialContext, and StartNodeId are required to start a process.");
    }
    return new ProcessInstance(ProcessInstanceId.generate(), definitionId, initialContext, startNodeId);
  }

  @Override
  protected void validateInvariants() {

  }

  public void moveToNextNode(ProcessNodeId nextNodeId, BusinessContext updatedContext) {
    ensureIsRunning();
    this.currentNodeId = nextNodeId;
    this.context = updatedContext;
    // 如果nextNodeId为null，表示流程结束
    if (nextNodeId == null) {
      complete();
    }
  }

  public void fail(BusinessContext updatedContext) {
    ensureIsRunning();
    this.status = ProcessInstanceStatus.FAILED;
    this.context = updatedContext;
  }

  private void complete() {
    ensureIsRunning();
    this.status = ProcessInstanceStatus.COMPLETED;
  }

  private void ensureIsRunning() {
    if (this.status != ProcessInstanceStatus.RUNNING) {
      throw new IllegalStateException("Process instance is not in a RUNNING state. Current state: " + this.status);
    }
  }

  public enum ProcessInstanceStatus {
    RUNNING, COMPLETED, FAILED, SUSPENDED
  }
}
