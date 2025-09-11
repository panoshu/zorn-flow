package com.zornflow.interfaces.web.controller;

import com.zornflow.application.dto.processchain.CreateProcessChainRequest;
import com.zornflow.application.dto.processchain.ProcessChainResponse;
import com.zornflow.application.service.ProcessChainAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

/**
 * 提供流程链（ProcessChain）管理功能的RESTful API端点。
 */
@RestController
@RequestMapping("/api/admin/process-chains")
@RequiredArgsConstructor
public class ProcessChainAdminController {

  private final ProcessChainAdminService processChainAdminService;

  /**
   * API端点：创建一个新的流程链。
   *
   * @param request 包含新流程链数据的请求体
   * @return 成功时返回 201 Created 和新创建的流程链数据
   */
  @PostMapping
  public ResponseEntity<ProcessChainResponse> createProcessChain(@Valid @RequestBody CreateProcessChainRequest request) {
    ProcessChainResponse response = processChainAdminService.createProcessChain(request);
    URI location = URI.create("/api/admin/process-chains/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  /**
   * API端点：根据ID获取一个流程链。
   *
   * @param id 流程链的ID
   * @return 成功时返回 200 OK 和流程链数据，找不到则返回 404 Not Found
   */
  @GetMapping("/{id}")
  public ResponseEntity<ProcessChainResponse> getProcessChainById(@PathVariable String id) {
    return processChainAdminService.getProcessChainById(id)
      .map(ResponseEntity::ok)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ProcessChain not found with id: " + id));
  }

  /**
   * API端点：根据ID删除一个流程链。
   *
   * @param id 流程链的ID
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProcessChain(@PathVariable String id) {
    processChainAdminService.deleteProcessChain(id);
  }
}
