package com.zornflow.infrastructure.adapter.handler;

import com.zornflow.domain.rule.service.HandlerExecutor;
import com.zornflow.domain.rule.service.HandlerExecutorFactory;
import com.zornflow.domain.rule.valueobject.Handler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 23:33
 **/

@Service
@RequiredArgsConstructor
public class HandlerExecutorFactoryImpl implements HandlerExecutorFactory {

  private final List<HandlerExecutor> executors;

  @Override
  public Optional<HandlerExecutor> getExecutor(Handler handler) {
    return executors.stream()
      .filter(executor -> executor.supports(handler))
      .findFirst();
  }
}
