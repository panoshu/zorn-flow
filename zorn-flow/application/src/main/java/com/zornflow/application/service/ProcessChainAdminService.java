package com.zornflow.application.service;

import com.zornflow.application.dto.processchain.CreateProcessChainRequest;
import com.zornflow.application.dto.processchain.ProcessChainResponse;
import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.entity.ProcessNode;
import com.zornflow.domain.process.repository.ProcessChainRepository;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessChainName;
import com.zornflow.infrastructure.mapper.ProcessDomainMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 负责流程链（ProcessChain）管理的应用服务。
 * 封装了创建、查询、删除等用例。
 */
@Service
@RequiredArgsConstructor
public class ProcessChainAdminService {

  private final ProcessChainRepository processChainRepository;
  private final ProcessDomainMapper processDomainMapper; // 注入重命名后的 ProcessDomainMapper

  /**
   * 创建一个新的流程链。
   *
   * @param request 创建请求的数据
   * @return 创建成功后的流程链响应对象
   */
  @Transactional
  public ProcessChainResponse createProcessChain(CreateProcessChainRequest request) {
    // 使用 Mapper 将 DTO 转换为领域实体
    List<ProcessNode> domainNodes = request.nodes().stream()
      .map(processDomainMapper::toDomain)
      .collect(Collectors.toList());

    ProcessChain processChain = ProcessChain.builder()
      .id(ProcessChainId.of(request.id()))
      .name(ProcessChainName.of(request.name()))
      .description(request.description())
      .nodes(domainNodes)
      .build();

    ProcessChain saved = processChainRepository.save(processChain);
    return toResponse(saved);
  }

  /**
   * 根据ID查询流程链。
   *
   * @param id 流程链的ID
   * @return 如果找到，则返回包含流程链信息的Optional
   */
  @Transactional(readOnly = true)
  public Optional<ProcessChainResponse> getProcessChainById(String id) {
    return processChainRepository.findById(ProcessChainId.of(id))
      .map(this::toResponse);
  }

  /**
   * 根据ID删除流程链。
   *
   * @param id 要删除的流程链的ID
   */
  @Transactional
  public void deleteProcessChain(String id) {
    processChainRepository.deleteById(ProcessChainId.of(id));
  }

  // --- 私有辅助方法 ---

  /**
   * 将领域实体 ProcessChain 转换为用于API响应的 DTO。
   */
  private ProcessChainResponse toResponse(ProcessChain processChain) {
    return new ProcessChainResponse(
      processChain.getId().value(),
      processChain.getName().value(),
      processChain.getDescription(),
      processChain.getVersion(),
      processChain.getAllNodes().stream().map(processDomainMapper::toDto).collect(Collectors.toList()),
      processChain.getCreatedAt(),
      processChain.getUpdatedAt()
    );
  }
}
