package com.zornflow.gateway.application;

import com.zornflow.gateway.domain.spi.LogPublisher;
import com.zornflow.gateway.infrastructure.model.RequestLog;
import com.zornflow.gateway.infrastructure.model.ResponseLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 15:50
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class LoggingService {

  private final LogPublisher activePublisher;

  /**
   * 异步、非阻塞地记录日志。
   * "触发并忘记"模式的实现。
   *
   * @param entry 日志条目
   */
  public void logRequestAsync(RequestLog entry) {
    activePublisher.publishRequest(entry)
      .onErrorResume(e -> {
        log.error("Failed to publish request log", e);
        return Mono.empty();
      })
      .subscribeOn(Schedulers.boundedElastic())
      .subscribe();
  }

  public void logResponseAsync(ResponseLog entry) {
    activePublisher.publishResponse(entry)
      .onErrorResume(e -> {
        log.error("Failed to publish response log", e);
        return Mono.empty();
      })
      .subscribeOn(Schedulers.boundedElastic())
      .subscribe();
  }
}
