package com.zornflow.application.dto.loan;

import com.zornflow.domain.loan.model.LoanApplication;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 用于API响应的贷款申请数据传输对象。
 * 封装了前端需要展示的核心业务数据和流程状态。
 */
public record LoanApplicationResponse(
  String applicationId,
  String applicantName,
  BigDecimal claimAmount,
  LoanApplication.ApplicationStatus status,
  String currentStep, // 当前所处的流程节点ID
  Instant createdAt,
  Instant updatedAt
) {
}
