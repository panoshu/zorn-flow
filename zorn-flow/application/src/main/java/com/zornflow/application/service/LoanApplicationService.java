package com.zornflow.application.service;

import com.zornflow.application.dto.loan.ApproveRequest;
import com.zornflow.application.dto.loan.LoanApplicationResponse;
import com.zornflow.application.dto.loan.SubmitLoanApplicationRequest;
import com.zornflow.application.exception.EntityNotFoundException;
import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.loan.model.LoanApplication;
import com.zornflow.domain.loan.model.LoanApplicationId;
import com.zornflow.domain.loan.repository.LoanApplicationRepository;
import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.entity.ProcessInstance;
import com.zornflow.domain.process.repository.ProcessChainRepository;
import com.zornflow.domain.process.repository.ProcessInstanceRepository;
import com.zornflow.domain.process.service.ProcessOrchestrationService;
import com.zornflow.domain.process.types.ProcessChainId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

  private final LoanApplicationRepository loanApplicationRepository;
  private final ProcessInstanceRepository processInstanceRepository;
  private final ProcessChainRepository processChainRepository;
  private final ProcessOrchestrationService processOrchestrationService;

  /**
   * 业务用例：提交一个新的贷款申请。
   * 创建业务聚合根，并启动一个流程实例来处理它。
   */
  @Transactional
  public LoanApplicationResponse submitNewApplication(SubmitLoanApplicationRequest request) {
    // 1. 创建业务聚合根
    LoanApplication application = new LoanApplication(request.applicantName(), request.claimAmount());

    // 2. 准备流程引擎的初始业务上下文
    Map<String, Object> initialData = new HashMap<>();
    initialData.put("claimAmount", request.claimAmount());
    initialData.put("policyInfo", request.policyInfo());
    initialData.put("accidentInfo", request.accidentInfo());
    BusinessContext initialContext = new BusinessContext(initialData);

    // 3. 查找并获取流程定义
    ProcessChainId processChainId = ProcessChainId.of("loan-process");
    ProcessChain processChain = processChainRepository.findById(processChainId)
      .orElseThrow(() -> new IllegalStateException("未找到贷款审批的流程定义: " + processChainId.value()));

    // 4. 启动一个新的流程实例
    ProcessInstance processInstance = ProcessInstance.start(
      processChain.getId(),
      initialContext,
      processChain.getStartNodeId()
    );

    // 5. 将业务聚合根与流程实例关联
    application.submit(processInstance.getId());

    // 6. 持久化两个聚合根的状态
    loanApplicationRepository.save(application);
    processInstanceRepository.save(processInstance);

    // 7. 立即驱动流程引擎执行第一步
    processOrchestrationService.executeNextStep(processInstance);

    // 8. 再次持久化流程实例的最新状态
    processInstanceRepository.save(processInstance);

    // 9. 转换为对外的响应 DTO
    return createResponse(application, processInstance);
  }

  /**
   * 业务用例：处理审批操作。
   */
  @Transactional
  public LoanApplicationResponse approveApplication(String applicationId, ApproveRequest request) {
    // 1. 加载业务聚合根
    LoanApplicationId id = LoanApplicationId.of(applicationId);
    LoanApplication application = loanApplicationRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("找不到ID为 " + applicationId + " 的贷款申请"));

    // 2. 加载关联的流程实例
    ProcessInstance processInstance = processInstanceRepository.findById(application.getProcessInstanceId())
      .orElseThrow(() -> new IllegalStateException("数据不一致：找不到关联的流程实例 " + application.getProcessInstanceId().value()));

    // 3. 校验当前流程节点是否是预期的审批节点 (此为可选的业务检查)
    // if (!"node4".equals(processInstance.getCurrentNodeId().value())) {
    //     throw new IllegalStateException("当前流程状态不正确，无法执行批准操作。");
    // }

    // 4. 更新业务上下文
    BusinessContext updatedContext = processInstance.getContext()
      .with("approvalComment", request.comment())
      .with("approver", request.approver());

    // 在驱动引擎前，先用新上下文更新实例状态（尽管引擎内部也会做）
    processInstance.moveToNextNode(processInstance.getCurrentNodeId(), updatedContext);

    // 5. 驱动流程引擎继续执行
    processOrchestrationService.executeNextStep(processInstance);

    // 6. 检查流程是否结束，并相应地更新业务聚合根的状态
    if (processInstance.getStatus() == ProcessInstance.ProcessInstanceStatus.COMPLETED) {
      application.approve(); // 假设流程成功结束即为批准
      loanApplicationRepository.save(application);
    }

    // 7. 持久化流程实例的最新状态
    processInstanceRepository.save(processInstance);

    return createResponse(application, processInstance);
  }

  /**
   * 私有辅助方法，用于将领域对象转换为响应DTO。
   */
  private LoanApplicationResponse createResponse(LoanApplication application, ProcessInstance instance) {
    String currentStep = (instance.getStatus() == ProcessInstance.ProcessInstanceStatus.COMPLETED)
      ? "COMPLETED"
      : instance.getCurrentNodeId().value();

    return new LoanApplicationResponse(
      application.getId().value(),
      application.getApplicantName(),
      application.getClaimAmount(),
      application.getStatus(),
      currentStep,
      application.getCreatedAt(),
      application.getUpdatedAt()
    );
  }
}
