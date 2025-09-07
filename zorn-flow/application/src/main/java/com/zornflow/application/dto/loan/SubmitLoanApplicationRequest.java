package com.zornflow.application.dto.loan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 提交新贷款申请的API请求数据模型。
 * 包含了所有启动一个贷款流程所需的业务数据。
 * 使用 @Valid 进行级联校验。
 */
public record SubmitLoanApplicationRequest(
  @NotBlank(message = "申请人姓名不能为空") @Size(max = 100) String applicantName,
  @NotNull(message = "贷款金额不能为空") @DecimalMin(value = "0.0", inclusive = false) BigDecimal claimAmount,
  @NotNull @Valid PolicyInfo policyInfo,
  @NotNull @Valid AccidentInfo accidentInfo
) {
  public record PolicyInfo(@NotBlank String policyId, @NotBlank String status) {}
  public record AccidentInfo(@NotNull Instant accidentTime) {}
}
