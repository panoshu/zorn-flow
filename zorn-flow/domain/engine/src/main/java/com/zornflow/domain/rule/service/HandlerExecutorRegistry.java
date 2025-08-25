package com.zornflow.domain.rule.service;

import java.util.List;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/1 12:20
 */

public interface HandlerExecutorRegistry {
  void ExecutorRegistry(List<HandlerExecutor> executorList);
  HandlerExecutor getExecutor(String actionType);
}
