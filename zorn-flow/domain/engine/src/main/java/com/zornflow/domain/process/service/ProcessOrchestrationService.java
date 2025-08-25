package com.zornflow.domain.process.service;

import com.zornflow.domain.process.entity.ProcessInstance;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 22:55
 **/

public interface ProcessOrchestrationService {
  void executeNextStep(ProcessInstance instance);
}
