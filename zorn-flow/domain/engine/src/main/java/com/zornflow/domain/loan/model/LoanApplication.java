package com.zornflow.domain.loan.model;

import com.domain.contract.aggregate.AggregateRoot;
import com.zornflow.domain.process.types.ProcessInstanceId;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/8 03:12
 **/

@Getter
public class LoanApplication extends AggregateRoot<LoanApplicationId> {

  // 业务核心数据
  private final String applicantName;
  private final BigDecimal claimAmount;
  private ApplicationStatus status;

  private ProcessInstanceId processInstanceId;

  // 构造器，用于创建
  public LoanApplication(String applicantName, BigDecimal claimAmount) {
    super(LoanApplicationId.generate());

    // 业务规则校验：金额必须为正数
    if (claimAmount == null || claimAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("贷款金额必须为正数。");
    }

    this.applicantName = applicantName;
    this.claimAmount = claimAmount;
    this.status = ApplicationStatus.DRAFT; // 初始状态为草稿
  }

  /**
   * 业务行为：提交申请
   * 这个行为会启动一个流程实例。
   */
  public void submit(ProcessInstanceId processInstanceId) {
    if (this.status != ApplicationStatus.DRAFT) {
      throw new IllegalStateException("只有草稿状态的申请才能被提交。");
    }
    this.processInstanceId = Objects.requireNonNull(processInstanceId);
    this.status = ApplicationStatus.SUBMITTED;
    // 未来可以在此注册领域事件，例如 LoanApplicationSubmittedEvent
  }

  /**
   * 业务行为：审批通过
   * 由流程引擎在特定节点回调触发
   */
  public void approve() {
    this.status = ApplicationStatus.APPROVED;
  }

  /**
   * 业务行为：审批拒绝
   */
  public void reject(String reason) {
    this.status = ApplicationStatus.REJECTED;
    // 可以记录拒绝原因
  }

  // ... 其他业务方法 ...

  @Override
  protected void validateInvariants() {
    // 可以在此添加更复杂的跨字段业务规则校验
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

  public enum ApplicationStatus {
    DRAFT, SUBMITTED, APPROVED, REJECTED
  }
}
