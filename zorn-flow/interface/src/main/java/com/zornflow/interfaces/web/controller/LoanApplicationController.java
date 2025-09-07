package com.zornflow.interfaces.web.controller;

import com.zornflow.application.dto.loan.ApproveRequest;
import com.zornflow.application.dto.loan.LoanApplicationResponse;
import com.zornflow.application.dto.loan.SubmitLoanApplicationRequest;
import com.zornflow.application.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications/loan")
@RequiredArgsConstructor
public class LoanApplicationController {

  private final LoanApplicationService loanApplicationService;

  @PostMapping("submit")
  public ResponseEntity<LoanApplicationResponse> submitApplication(@RequestBody SubmitLoanApplicationRequest request) {
    LoanApplicationResponse response = loanApplicationService.submitNewApplication(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * [新增] API 端点：批准一个贷款申请。
   *
   * @param applicationId 要批准的贷款申请的ID
   * @param request       包含审批人、评论等信息的请求体
   * @return 成功时返回 200 OK 和更新后的贷款申请数据
   */
  @PostMapping("/{applicationId}/approve")
  public ResponseEntity<LoanApplicationResponse> approveApplication(
    @PathVariable String applicationId,
    @Valid @RequestBody ApproveRequest request) {

    LoanApplicationResponse response = loanApplicationService.approveApplication(applicationId, request);
    return ResponseEntity.ok(response);
  }

}
